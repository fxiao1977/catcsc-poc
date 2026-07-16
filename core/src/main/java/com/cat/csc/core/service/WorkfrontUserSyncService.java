package com.cat.csc.core.service;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.sling.api.resource.ResourceResolver;

public interface WorkfrontUserSyncService {

    /**
     * Sync a Workfront user (wf-<email>) by:
     *  - finding matching AEM user by email
     *  - adding Workfront user to matching Consumer/Producer groups
     *  - removing Workfront user from wf-workfront-users only if matched
     */
    void syncUser(ResourceResolver resolver, Authorizable newUser, boolean fromScheduler) throws Exception;
}

