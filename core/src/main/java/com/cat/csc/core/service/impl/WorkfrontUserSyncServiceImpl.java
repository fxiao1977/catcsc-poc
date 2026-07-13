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

        boolean matched = false;
        UserManager userManager = resolver.adaptTo(UserManager.class);

        if (processGroups(resolver, userManager, sharedUserSyncConfigProvider.consumerGroups, newUser, email)) {
            matched = true;
        }

        if (processGroups(resolver, userManager, sharedUserSyncConfigProvider.producerGroups, newUser, email)) {
            matched = true;
        }

        if (matched) {
            Group wfGroup = (Group) userManager.getAuthorizable("wf-workfront-users");
            if (wfGroup != null && wfGroup.isMember(newUser)) {
                boolean isUserRemoved = wfGroup.removeMember(newUser);
                if(isUserRemoved){
                    Session session = resolver.adaptTo(Session.class);
                    if (session != null) {
                        session.save();
                    }
                    log.info("Removed {} from wf-workfront-users (match found)", newUserId);
                }

            }
        } else {
            log.warn("No matching AEM user found for Workfront user {} — keeping in wf-workfront-users", newUserId);
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

