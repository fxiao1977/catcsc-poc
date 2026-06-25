package com.cat.csc.core.service;

import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.RepositoryException;
import javax.jcr.Session;


public interface PermissionSyncService {
    void syncFolderPermissions(Session session, ResourceResolver resourceResolver, String folderPath) throws RepositoryException;
}
