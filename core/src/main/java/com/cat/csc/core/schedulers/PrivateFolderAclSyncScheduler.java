package com.cat.csc.core.schedulers;

import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;

import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.*;

import javax.jcr.*;
import javax.jcr.security.*;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jackrabbit.api.security.user.*;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlList;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlEntry;

import com.day.cq.search.QueryBuilder;
import com.day.cq.search.Query;
import com.day.cq.search.result.SearchResult;
import com.day.cq.search.result.Hit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.search.PredicateGroup;
import com.cat.csc.core.service.PermissionSyncService;

@Component(service = Runnable.class, immediate = true)
@Designate(ocd = PrivateFolderAclSyncSchedulerConfiguration.class)
public class PrivateFolderAclSyncScheduler implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrivateFolderAclSyncScheduler.class);

    @Reference
    private Scheduler scheduler;

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private QueryBuilder queryBuilder;

    @Reference
    private PermissionSyncService permissionSyncService;

    private PrivateFolderAclSyncSchedulerConfiguration config;

    private String jobName = PrivateFolderAclSyncScheduler.class.getName();
    private AtomicBoolean running = new AtomicBoolean(false);


    @Activate
    @Modified
    protected void activate(final PrivateFolderAclSyncSchedulerConfiguration config) {
         addScheduler(config);
    }

    private void addScheduler(PrivateFolderAclSyncSchedulerConfiguration config) {

        this.config = config;
        if (!config.enable_scheduler()){
            LOGGER.info("CATCSC Permission Sync Scheduler is disabled.");
            return;
        }

        ScheduleOptions options = scheduler.EXPR(config.scheduler_expression());
        options.name(config.scheduler_name());

        options.canRunConcurrently(config.concurrent_scheduler());

        scheduler.schedule(this, options);
        LOGGER.info("CATCSC Permission Sync Scheduler activated. Cron={}, Root={}",
                config.scheduler_expression(), config.rootPath());
    }


    // Custom method to deactivate or unschedule scheduler
    public void removeScheduler(PrivateFolderAclSyncSchedulerConfiguration config) {
        scheduler.unschedule(config.scheduler_name());
    }

    // On deactivate component it will unschedule scheduler
    @Deactivate
    protected void deactivate(PrivateFolderAclSyncSchedulerConfiguration config) {
        removeScheduler(config);
    }

    // On component modification change status will remove and add scheduler
    @Modified
    protected void modified(PrivateFolderAclSyncSchedulerConfiguration config) {
        removeScheduler(config);
        addScheduler(config);
    }

    @Override
    public void run() {

        if(!config.enable_scheduler()){
            return;
        }

        LOGGER.info("CATCSC Permission Sync Scheduler started.");

        Map<String, Object> param = Collections.singletonMap(
                ResourceResolverFactory.SUBSERVICE, "private-folder-service"
        );

        try (ResourceResolver resolver = resolverFactory.getServiceResourceResolver(param)) {
            Session session = resolver.adaptTo(Session.class);
            if (session == null) {
                return;
            }

            processPrivateFolders(session, resolver);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            running.set(false);
        }
    }

    // ---------------------------------------------------------
    // Find private folders using Query Builder
    // ---------------------------------------------------------
    private void processPrivateFolders(Session session, ResourceResolver resolver)
            throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put("path", config.rootPath());
        params.put("type", "sling:Folder");
        params.put("nodename", "*");
        params.put("property", "rep:policy/deny/rep:principalName");
        params.put("property.value", "everyone");
        params.put("p.limit", "-1");

        PredicateGroup predicates = PredicateGroup.create(params);
        Query query = queryBuilder.createQuery(predicates, session);

        SearchResult result = query.getResult();

        //Sync permission on private folders
        int count = 0;
        for (Hit hit : result.getHits()) {
            String folderPath = hit.getPath();
            try {
                permissionSyncService.syncFolderPermissions(session, resolver, folderPath);
                count++;
            } catch (Exception e) {
                LOGGER.error("Error syncing folder {}: {}", folderPath, e.getMessage(), e);
            }
        }

        session.save();
        LOGGER.info("CATCSC Permission Sync Scheduler completed. Synced {} folder(s).", count);
    }
}
