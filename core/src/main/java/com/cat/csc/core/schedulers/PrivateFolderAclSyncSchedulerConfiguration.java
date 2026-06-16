package com.cat.csc.core.schedulers;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "CATCSC Private Folder Permission Update Scheduler",
        description = "For private folders, apply same permissions to Workfront ID. "
)
public @interface PrivateFolderAclSyncSchedulerConfiguration {

    @AttributeDefinition(
            name = "Enable Scheduler",
            description = "Enable Scheduler",
            type = AttributeType.BOOLEAN)
    boolean enable_scheduler() default true;

    @AttributeDefinition(
            name = "Scheduler name",
            description = "Scheduler name",
            type = AttributeType.STRING)
    String scheduler_name() default "Private Folders Permission Update";

    // cron job for every minute
    @AttributeDefinition(
            name = "Cron job expression",
            description = "Cron job expression",
            type = AttributeType.STRING)
    String scheduler_expression() default "0 * * * * ?";

    @AttributeDefinition(
            name = "Concurrent Scheduler",
            description = "Concurrent Scheduler",
            type = AttributeType.BOOLEAN)
    boolean concurrent_scheduler() default false;

    @AttributeDefinition(
            name = "Scan Root Path",
            description = "Root path under which to search for private folders (e.g. /content/dam)",
            type = AttributeType.STRING)
    String rootPath() default "/content/dam";
}
