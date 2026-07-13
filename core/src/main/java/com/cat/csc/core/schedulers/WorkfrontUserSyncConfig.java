package com.cat.csc.core.schedulers;

import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.AttributeDefinition;


@ObjectClassDefinition(
        name = "CATCSC Workfront User Sync Configuration",
        description = "Controls Workfront user permission synchronization"
)
public @interface WorkfrontUserSyncConfig {

    @AttributeDefinition(
            name = "Enable Workfront User Sync Listener",
            description = "Enable or disable automatic sync when a new Workfront user is added"
    )
    boolean enabled() default false;

    @AttributeDefinition(
            name = "Enable Scheduled Sync Job",
            description = "Enable or disable scheduled scanning of all Workfront users"
    )
    boolean jobEnabled() default false;

    @AttributeDefinition(
            name = "Scheduled Job Cron",
            description = "Cron expression for the scheduled job",
            defaultValue = "0 0 * * * ?" // every 10 minutes
    )
    String jobCron();

    @AttributeDefinition(
            name = "Consumer Groups",
            description = "List of consumer groups to search for matching AEM users"
    )
    String[] consumerGroups() default {
            "Consumers",
            "CAN Only Consumers",
            "WCM_Assets_Consumers",
            "BCP Marcom - Consumers"
    };

    @AttributeDefinition(
            name = "Producer Groups",
            description = "List of producer groups to search for matching AEM users"
    )
    String[] producerGroups() default {
            "Producers",
            "CAT Only Producers",
            "BCP Marcom - Producers",
            "WCM_Assets_Producers"
    };

    @AttributeDefinition(
            name = "Concurrent Scheduler",
            description = "Concurrent Scheduler",
            type = AttributeType.BOOLEAN)
    boolean concurrent_scheduler() default false;

    @AttributeDefinition(
            name = "Scheduler name",
            description = "Scheduler name",
            type = AttributeType.STRING)
    String scheduler_name() default "Workfront User Sync Scheduler";
}

