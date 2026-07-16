package com.cat.csc.core.service.impl;

import com.cat.csc.core.service.SharedUserSyncConfigProvider;
import com.cat.csc.core.service.WorkfrontUserSyncService;
import org.apache.jackrabbit.api.security.user.*;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import javax.jcr.Value;
import java.util.Iterator;

@Component(service = WorkfrontUserSyncService.class)
public class WorkfrontUserSyncServiceImpl implements WorkfrontUserSyncService {

    @Reference
    private SharedUserSyncConfigProvider sharedUserSyncConfigProvider;

    private static final Logger log = LoggerFactory.getLogger(WorkfrontUserSyncServiceImpl.class);

    @Override
    public void syncUser(ResourceResolver resolver,  Authorizable newUser, boolean fromScheduler) throws Exception {

        String newUserId = newUser.getID();
        if (!newUserId.startsWith("wf-")) {
            return;
        }

        String email = newUserId.substring(3);
        log.info("Processing Workfront user {} (email={})", newUserId, email);

        boolean consumer_group_matched = false;
        boolean producer_group_matched = false;
        UserManager userManager = resolver.adaptTo(UserManager.class);

        if (processGroups(resolver, userManager, sharedUserSyncConfigProvider.getConsumerGroups(), newUser, email, fromScheduler)) {
            consumer_group_matched = true;
        }

        if (processGroups(resolver, userManager, sharedUserSyncConfigProvider.getProducerGroups(), newUser, email, fromScheduler)) {
            producer_group_matched = true;
        }

        if (!consumer_group_matched && !producer_group_matched) {
            log.warn("No matching AEM user found for Workfront user {} ", newUserId);
        }
    }

    private boolean processGroups(ResourceResolver resourceResolver, UserManager userManager, String[] groupNames, Authorizable wfUser, String email,boolean fromScheduler) throws Exception {

        boolean matched = false;
        for (String groupName : groupNames) {

            Authorizable groupAuth = userManager.getAuthorizable(groupName);

            if (groupAuth == null || !groupAuth.isGroup()) {
                log.warn("Group {} does not exist or is not a valid group", groupName);
                continue;
            }

            Group group = (Group) groupAuth;
            boolean shouldBeMember = false;
            Iterator<Authorizable> members = group.getMembers();

            while (members.hasNext()) {
                Authorizable member = members.next();

                Value[] emailProp = member.getProperty("profile/email");

                if (emailProp != null && emailProp.length > 0) {

                    String memberEmail = emailProp[0].getString();

                    if (email.equalsIgnoreCase(memberEmail)) {

                        if (!group.isMember(wfUser)) {
                            boolean userAdded = group.addMember(wfUser);
                            if(userAdded){
                                Session session = resourceResolver.adaptTo(Session.class);
                                if (session != null) {
                                    session.save();
                                }
                                log.info("Added {} to group {}", wfUser.getID(), groupName);
                            }
                        }
                        shouldBeMember = true;
                        matched = true;
                    }
                }
            }

            //remove outdated membership (scheduler only)
            if (fromScheduler && !shouldBeMember && group.isMember(wfUser)) {
                boolean userRemoved = group.removeMember(wfUser);
                if(userRemoved){
                    Session session = resourceResolver.adaptTo(Session.class);
                    if (session != null) {
                        session.save();
                    }
                    log.info("Removed Workfront user {} from group {} (AEM user no longer in group)", wfUser.getID(), groupName);
                }
            }
        }

        return matched;
    }
}

