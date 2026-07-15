package com.cat.csc.core.service.impl;

import com.cat.csc.core.schedulers.WorkfrontUserSyncConfig;
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
    public void syncUser(ResourceResolver resolver,  Authorizable newUser) throws Exception {

        String newUserId = newUser.getID();
        if (!newUserId.startsWith("wf-")) {
            return;
        }

        String email = newUserId.substring(3);
        log.info("Processing Workfront user {} (email={})", newUserId, email);

        boolean consumer_group_matched = false;
        boolean producer_group_matched = false;
        UserManager userManager = resolver.adaptTo(UserManager.class);

        if (processGroups(resolver, userManager, sharedUserSyncConfigProvider.consumerGroups, newUser, email)) {
            consumer_group_matched = true;
        }

        if (processGroups(resolver, userManager, sharedUserSyncConfigProvider.producerGroups, newUser, email)) {
            producer_group_matched = true;
        }

        if (!consumer_group_matched && !producer_group_matched) {
            log.warn("No matching AEM user found for Workfront user {} ", newUserId);
        }
    }

    private boolean processGroups(ResourceResolver resourceResolver, UserManager userManager, String[] groupNames, Authorizable newUser, String email) throws Exception {

        boolean matched = false;
        for (String groupName : groupNames) {

            Authorizable groupAuth = userManager.getAuthorizable(groupName);

            if (groupAuth == null || !groupAuth.isGroup()) {
                continue;
            }

            Group group = (Group) groupAuth;

            Iterator<Authorizable> members = group.getMembers();

            while (members.hasNext()) {
                Authorizable member = members.next();

                Value[] emailProp = member.getProperty("profile/email");

                if (emailProp != null && emailProp.length > 0) {

                    String memberEmail = emailProp[0].getString();

                    if (email.equalsIgnoreCase(memberEmail)) {

                        if (!group.isMember(newUser)) {
                            boolean userAdded = group.addMember(newUser);
                            if(userAdded){
                                Session session = resourceResolver.adaptTo(Session.class);
                                if (session != null) {
                                    session.save();
                                }
                                log.info("Added {} to group {}", newUser.getID(), groupName);
                            }
                        }

                        matched = true;
                    }
                }
            }
        }

        return matched;
    }
}

