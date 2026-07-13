package com.cat.csc.core.service;

import com.cat.csc.core.schedulers.PrivateFolderAclSyncSchedulerConfiguration;
import com.cat.csc.core.schedulers.WorkfrontUserSyncConfig;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ResourceResolver;

public interface WorkfrontUserSyncService {

    /**
     * Sync a Workfront user (wf-<email>) by:
     *  - finding matching AEM user by email
     *  - adding Workfront user to matching Consumer/Producer groups
     *  - removing Workfront user from wf-workfront-users only if matched
     */
    void syncUser(ResourceResolver resolver, Authorizable newUser) throws Exception;
}

