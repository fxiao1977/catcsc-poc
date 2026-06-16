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

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * OSGi configuration for PrivateFolderPermissionChangeListener.
 * This configuration allows administrators to enable/disable the listener
 * and configure the root path to monitor for permission changes.
 */
@ObjectClassDefinition(
    name = "CAT CSC - Private Folder Permission Change Listener Configuration",
    description = "Configuration for JCR event listener that monitors permission changes on private folders"
)
public @interface PrivateFolderPermissionChangeListenerConfig {

    /**
     * Enable or disable the permission change listener.
     * When disabled, the listener will not be activated and no events will be monitored.
     * Default is true.
     */
    @AttributeDefinition(
        name = "Enabled",
        description = "Enable or disable the permission change listener",
        type = AttributeType.BOOLEAN
    )
    boolean enabled() default true;

    /**
     * The root path to monitor for permission changes.
     * The listener will monitor all nested paths under this root.
     * Default is /content/dam.
     * 
     * Examples:
     * - /content/dam (monitor all DAM content)
     * - /content/dam/private (monitor only private folders)
     * - /content (monitor all content)
     */
    @AttributeDefinition(
        name = "Monitored Root Path",
        description = "The repository path to monitor for permission changes (child paths are also monitored)",
        type = AttributeType.STRING
    )
    String monitoredRootPath() default "/content/dam";

    /**
     * Enable monitoring of PROPERTY_CHANGED events.
     * These events are triggered when permission properties are modified.
     * Default is true.
     */
    @AttributeDefinition(
        name = "Monitor Property Changes",
        description = "Monitor PROPERTY_CHANGED events (permission property modifications)",
        type = AttributeType.BOOLEAN
    )
    boolean monitorPropertyChanges() default true;

    /**
     * Enable monitoring of PROPERTY_ADDED events.
     * These events are triggered when new permission properties are added.
     * Default is true.
     */
    @AttributeDefinition(
        name = "Monitor Property Additions",
        description = "Monitor PROPERTY_ADDED events (new permission properties)",
        type = AttributeType.BOOLEAN
    )
    boolean monitorPropertyAdditions() default true;

    /**
     * Enable monitoring of PROPERTY_REMOVED events.
     * These events are triggered when permission properties are removed.
     * Default is true.
     */
    @AttributeDefinition(
        name = "Monitor Property Removals",
        description = "Monitor PROPERTY_REMOVED events (removed permission properties)",
        type = AttributeType.BOOLEAN
    )
    boolean monitorPropertyRemovals() default true;

    /**
     * Enable monitoring of NODE_ADDED events.
     * These events are triggered when nodes are added.
     * Default is true.
     */
    @AttributeDefinition(
            name = "Monitor Node Added",
            description = "Monitor NODE_ADDED events (node added)",
            type = AttributeType.BOOLEAN
    )
    boolean monitorNodeAdded() default true;

    /**
     * Enable monitoring of NODE_REMOVED events.
     * These events are triggered when nodes are removed.
     * Default is true.
     */
    @AttributeDefinition(
            name = "Monitor Node Removed",
            description = "Monitor NODE_REMOVED events (node removed)",
            type = AttributeType.BOOLEAN
    )
    boolean monitorNodeRemoved() default true;

    /**
     * Enable monitoring of nested paths.
     * When true, the listener will monitor all child paths under the root path.
     * When false, only direct changes to the root path are monitored.
     * Default is true.
     */
    @AttributeDefinition(
        name = "Monitor Nested Paths",
        description = "Monitor nested child paths under the root path",
        type = AttributeType.BOOLEAN
    )
    boolean monitorNestedPaths() default true;

    /**
     * Logger level for the listener.
     * Controls the verbosity of logging output from the listener.
     */
    @AttributeDefinition(
        name = "Logger Level",
        description = "Logging level (DEBUG, INFO, WARN, ERROR)",
        type = AttributeType.STRING
    )
    String logLevel() default "INFO";
}

