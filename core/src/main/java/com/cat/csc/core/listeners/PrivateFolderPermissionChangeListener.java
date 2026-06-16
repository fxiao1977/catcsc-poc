/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.cat.csc.core.listeners;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

/**
 * A JCR event listener that monitors permission changes on folders under
 * a configurable root path. This listener detects when access control lists or
 * permission properties change on DAM resources.
 * 
 * The listener can be enabled/disabled and the monitored path can be configured
 * via OSGi configuration.
 */
@Component(service = PrivateFolderPermissionChangeListener.class, immediate = true)
@Designate(ocd = PrivateFolderPermissionChangeListenerConfig.class)
public class PrivateFolderPermissionChangeListener implements EventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrivateFolderPermissionChangeListener.class);

    private static final String REP_POLICY = "rep:policy";
    private static final String ACL_PATH = "/jcr:content/rep:policy";

    @Reference
    private SlingRepository repository;

    @Reference
    private ResourceResolverFactory resolverFactory;

    private ResourceResolver resourceResolver;
    private ObservationManager observationManager;
    private Session observationSession;
    private PrivateFolderPermissionChangeListenerConfig config;
    private String monitoredPath;

    /**
     * Initializes the JCR event listener by setting up observation on the configured root path.
     * This method is called when the component is activated.
     * 
     * @param configuration The OSGi configuration for this listener
     */
    @Activate
    protected void activate(final PrivateFolderPermissionChangeListenerConfig configuration) {
        this.config = configuration;
        
        // Check if the listener is enabled
        if (!config.enabled()) {
            LOGGER.info("PrivateFolderPermissionChangeListener is disabled via configuration");
            return;
        }
        
        // Set the monitored path from configuration
        this.monitoredPath = config.monitoredRootPath();
        
        try {

            Map<String, Object> param = Collections.singletonMap(
                    ResourceResolverFactory.SUBSERVICE, "private-folder-service"
            );
            // Get the service resource resolver
            resourceResolver = resolverFactory.getServiceResourceResolver(param);
            // Adapt the ResourceResolver to get the JCR Session
            observationSession = resourceResolver.adaptTo(Session.class);
            if (observationSession != null) {
                observationManager = observationSession.getWorkspace().getObservationManager();

                // Set up event listener for property changes
                int eventTypes = buildEventTypeMask();
                String[] nodeTypes = {"rep:policy"};
                boolean deep = config.monitorNestedPaths();
                boolean noLocal = true;

                observationManager.addEventListener(
                        this,
                        eventTypes,
                        monitoredPath,
                        deep,
                        null,  //uuid
                        null, //nodeTypes: null means every node
                        false //noLocal
                );

                LOGGER.info("PrivateFolderPermissionChangeListener activated. Listening for permission changes under path: {}, " +
                                "deep: {}, event types: [PROPERTY_CHANGED: {}, PROPERTY_ADDED: {}, PROPERTY_REMOVED: {}]",
                        monitoredPath,
                        deep,
                        config.monitorPropertyChanges(),
                        config.monitorPropertyAdditions(),
                        config.monitorPropertyRemovals());
            } else {
                LOGGER.error("Failed to adapt ResourceResolver to Session for PrivateFolderPermissionChangeListener");
            }

        } catch (RepositoryException e) {
            LOGGER.error("Failed to activate PrivateFolderPermissionChangeListener", e);
        } catch (LoginException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Builds the event type mask based on configuration settings.
     * 
     * @return An integer bitmask representing which event types to monitor
     */
    private int buildEventTypeMask() {
        int eventTypes = 0;
        
        if (config.monitorPropertyChanges()) {
            eventTypes |= Event.PROPERTY_CHANGED;
        }
        if (config.monitorPropertyAdditions()) {
            eventTypes |= Event.PROPERTY_ADDED;
        }
        if (config.monitorPropertyRemovals()) {
            eventTypes |= Event.PROPERTY_REMOVED;
        }

        if (config.monitorPropertyChanges()) {
            eventTypes |= Event.NODE_ADDED;
        }

        if (config.monitorPropertyRemovals()) {
            eventTypes |= Event.NODE_REMOVED;
        }
        
        // Default to PROPERTY_CHANGED if no events are selected
        if (eventTypes == 0) {
            eventTypes = Event.PROPERTY_CHANGED;
        }
        
        return eventTypes;
    }

    /**
     * Deactivates the JCR event listener and cleans up resources.
     * This method is called when the component is deactivated.
     */
    @Deactivate
    protected void deactivate() {
        try {
            if (observationSession != null) {
                ObservationManager observationManager = observationSession.getWorkspace().getObservationManager();
                observationManager.removeEventListener(this);
                observationSession.logout();
                observationSession = null;
                LOGGER.info("PrivateFolderPermissionChangeListener deactivated");
            }
        } catch (RepositoryException e) {
            LOGGER.error("Error during deactivation of PrivateFolderPermissionChangeListener", e);
        }
        
        config = null;
        monitoredPath = null;
    }

    /**
     * Called when JCR events matching the registered criteria occur.
     * Filters and processes permission-related changes.
     *
     * @param events An iterator of the events that occurred
     */
    @Override
    public void onEvent(EventIterator events) {
        while (events.hasNext()) {
            Event event = events.nextEvent();
            try {
                String eventPath = event.getPath();
                int eventType = event.getType();

                String path = event.getPath();

                // Only react to ACL changes
                if (path.contains("rep:policy")) {
                    // Your logic here
                    System.out.println(" ----- ACL changed at: " + path);
                }

                // Filter for ACL/permission related changes
                if (isPermissionRelatedChange(eventPath, eventType)) {
                    handlePermissionChange(event);
                }
            } catch (RepositoryException e) {
                LOGGER.error("Error processing JCR event", e);
            }
        }
    }

    /**
     * Determines if a change is permission-related by checking if the path
     * or property name indicates an ACL or permission change.
     *
     * @param path The path of the node where the event occurred
     * @param eventType The type of event that occurred
     * @return true if the change is permission-related, false otherwise
     */
    private boolean isPermissionRelatedChange(String path, int eventType) {
        // Check if the path contains rep:policy (Access Control List path)
        if (path.contains(REP_POLICY)) {
            LOGGER.debug("Permission change detected at path: {} (event type: {})", path, eventType);
            return true;
        }

        // Additional check for permission-related properties
        // You can extend this to check for other permission-related properties
        String[] permissionProperties = {
            "jcr:access",
            "rep:principalName",
            "rep:glob",
            "rep:restrictions",
            "jcr:uuid"
        };

        for (String propName : permissionProperties) {
            if (path.endsWith(propName)) {
                LOGGER.debug("Permission-related property change: {} at path: {}", propName, path);
                return true;
            }
        }

        return false;
    }

    /**
     * Processes a permission change event.
     * Log the change and optionally trigger additional business logic.
     *
     * @param event The JCR event that occurred
     * @throws RepositoryException If an error occurs while accessing the repository
     */
    private void handlePermissionChange(Event event) throws RepositoryException {
        String path = event.getPath();
        int eventType = event.getType();
        String eventTypeStr = getEventTypeString(eventType);
        String userId = event.getUserID();

        LOGGER.info("Permission change event - Type: {}, Path: {}, User: {}", eventTypeStr, path, userId);

        // Extract the folder path (parent path if dealing with jcr:content/rep:policy)
        String folderPath = extractFolderPath(path);
        LOGGER.debug("Affected folder: {}", folderPath);

        // TODO: Add your custom business logic here
        // Examples:
        // - Send notifications
        // - Audit logging
        // - Synchronize with external systems
        // - Update cache or indexes
        // - Trigger workflow
    }

    /**
     * Extracts the actual folder path from an event path.
     * Handles both direct policy paths and property paths under policies.
     *
     * @param eventPath The event path
     * @return The folder path
     */
    private String extractFolderPath(String eventPath) {
        if (eventPath.contains(ACL_PATH)) {
            // Remove the /jcr:content/rep:policy suffix
            return eventPath.replace(ACL_PATH, "");
        } else if (eventPath.contains(REP_POLICY)) {
            // Handle other rep:policy variants
            return eventPath.replaceAll("/rep:policy.*", "");
        }
        return eventPath;
    }

    /**
     * Converts event type constant to a human-readable string.
     *
     * @param eventType The event type constant
     * @return A string representation of the event type
     */
    private String getEventTypeString(int eventType) {
        switch (eventType) {
            case Event.NODE_ADDED:
                return "NODE_ADDED";
            case Event.NODE_REMOVED:
                return "NODE_REMOVED";
            case Event.NODE_MOVED:
                return "NODE_MOVED";
            case Event.PROPERTY_ADDED:
                return "PROPERTY_ADDED";
            case Event.PROPERTY_CHANGED:
                return "PROPERTY_CHANGED";
            case Event.PROPERTY_REMOVED:
                return "PROPERTY_REMOVED";
            default:
                return "UNKNOWN";
        }
    }
}











