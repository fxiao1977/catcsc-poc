package com.cat.csc.core.listeners;

import com.cat.csc.core.schedulers.WorkfrontUserSyncConfig;
import com.cat.csc.core.service.WorkfrontUserSyncService;
import com.cat.csc.core.service.impl.SharedUserSyncConfigProvider;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import java.util.*;

@Component(immediate = true)
public class WorkfrontGoupChangeListener implements EventListener {

    @Reference
    private ResourceResolverFactory resolverFactory;

    private static final Logger log = LoggerFactory.getLogger(WorkfrontGoupChangeListener.class);
    private static final String TARGET_GROUP = "wf-workfront-users";

    private ResourceResolver resolver;
    private Session session;

    @Reference
    private SharedUserSyncConfigProvider sharedUserSyncConfigProvider;

    @Reference
    private WorkfrontUserSyncService syncService;

    @Activate
    protected void activate() {

        try {
            Map<String, Object> param = Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, "catcsc-permission-service");
            resolver = resolverFactory.getServiceResourceResolver(param);
            session = resolver.adaptTo(Session.class);

            if (session != null) {
                // Listen strictly to property modifications (PROPERTY_ADDED / PROPERTY_CHANGED) under /home/groups
                session.getWorkspace().getObservationManager().addEventListener(
                        this,
                        Event.PROPERTY_ADDED | Event.PROPERTY_CHANGED,
                        "/home/groups",
                        true, // Is deep
                        null, // UUID filters
                        null, // Node type filters
                        false // No local flag
                );
                log.info("Successfully registered JCR Group Membership Listener.");
            }
        } catch (Exception e) {
            log.error("Failed to register JCR Event Listener", e);
        }
    }

    @Override
    public void onEvent(EventIterator events) {
        if(!sharedUserSyncConfigProvider.isEnabled()){
            return;
        }

        String exactGroupPath = null;
        try {
            UserManager userManager = resolver.adaptTo(UserManager.class);
            if (userManager == null) return;

            // Fetch the concrete target group reference directly
            Authorizable targetAuth = userManager.getAuthorizable(TARGET_GROUP);
            if (targetAuth == null || !targetAuth.isGroup()) return;
            Group targetGroup = (Group) targetAuth;

            while (events.hasNext()) {
                Event event = events.nextEvent();
                String propertyPath = event.getPath();

                if(!propertyPath.startsWith(targetGroup.getPath())){
                    return;
                }

                // 1. Confirm the modified JCR property is 'rep:members'
                if (propertyPath.endsWith("/rep:members")) {

                    // 2. Extract Event Info metadata mapping to the changed values
                    Map<?, ?> info = event.getInfo();
                    if (info != null) {

                        // 1. Extract raw arrays (handle potential nulls for new or deleted items)
                        Value[] beforeArray = (Value[]) event.getInfo().get("beforeValue");
                        Value[] afterArray = (Value[]) event.getInfo().get("afterValue");

                        Set<String> beforeSet = convertToStringSet(beforeArray);
                        Set<String> afterSet = convertToStringSet(afterArray);

                        // 2. Identify elements that were ADDED
                        Set<String> addedValues = new HashSet<>(afterSet);
                        addedValues.removeAll(beforeSet);
                        Iterator<String> addedMembers = addedValues.iterator();
                        while (addedMembers.hasNext()){
                                String memberUuid = addedMembers.next();
                                // 3. Resolve the structural JCR UUID directly to the User ID
                                javax.jcr.Node memberNode = session.getNodeByIdentifier(memberUuid);
                                String userId = memberNode.getProperty("rep:principalName").getString();
                                Authorizable newWorkfrontUser = userManager.getAuthorizable(userId);
                                syncService.syncUser(resolver, newWorkfrontUser);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error processing JCR membership event", e);
        }
    }

    // --- Helper Method for Safe Conversion ---
    private Set<String> convertToStringSet(Value[] values) {
        Set<String> resultSet = new HashSet<>();
        if (values != null) {
            for (Value val : values) {
                try {
                    resultSet.add(val.getString());
                } catch (Exception e) {
                    // Handle or log JCR RepositoryException
                }
            }
        }
        return resultSet;
    }

}