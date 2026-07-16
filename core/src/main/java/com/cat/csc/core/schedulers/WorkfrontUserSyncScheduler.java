package com.cat.csc.core.schedulers;

import com.cat.csc.core.service.WorkfrontUserSyncService;
import com.cat.csc.core.service.SharedUserSyncConfigProvider;
import org.apache.jackrabbit.api.security.user.*;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

@Component(service = Runnable.class, immediate = true)
public class WorkfrontUserSyncScheduler implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(WorkfrontUserSyncScheduler.class);

    @Reference
    private WorkfrontUserSyncService syncService;

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private SharedUserSyncConfigProvider sharedUserSyncConfigProvider;

    @Reference
    private Scheduler scheduler;

    @Activate
    @Modified
    protected void activate() {
        addScheduler();
    }

    private void addScheduler() {

        if (!sharedUserSyncConfigProvider.isJobEnabled()){
            log.info("CATCSC User Sync Scheduler is disabled.");
            return;
        }

        ScheduleOptions options = scheduler.EXPR(sharedUserSyncConfigProvider.getJobCron());
        options.name(sharedUserSyncConfigProvider.getScheduler_name());

        options.canRunConcurrently(sharedUserSyncConfigProvider.isConcurrent_scheduler());

        scheduler.schedule(this, options);
        log.info("CATCSC Permission Sync Scheduler activated. Cron={}",
                sharedUserSyncConfigProvider.getJobCron());
    }

    @Override
    public void run() {

        try {
            if (!sharedUserSyncConfigProvider.isJobEnabled()) {
                log.debug("Workfront sync scheduler disabled");
                return;
            }

            Map<String, Object> param = Collections.singletonMap(
                    ResourceResolverFactory.SUBSERVICE, "catcsc-permission-service"
            );

            ResourceResolver resolver = resolverFactory.getServiceResourceResolver(param);
            UserManager userManager = resolver.adaptTo(UserManager.class);

            Group wfGroup = (Group) userManager.getAuthorizable("wf-workfront-users");

            if (wfGroup == null) {
                return;
            }

            Iterator<Authorizable> members = wfGroup.getMembers();

            while (members.hasNext()) {
                Authorizable wfUser = members.next();
                syncService.syncUser(resolver, wfUser, true);
            }

        } catch (Exception e) {
            log.error("Error running WorkfrontUserSyncScheduler", e);
        }
    }
}

