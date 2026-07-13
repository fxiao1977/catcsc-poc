package com.cat.csc.core.service.impl;

import com.cat.csc.core.schedulers.WorkfrontUserSyncConfig;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Modified;


@Component(service = SharedUserSyncConfigProvider.class, immediate = true)
@Designate(ocd = WorkfrontUserSyncConfig.class)
public class SharedUserSyncConfigProvider {

    boolean enabled;
    boolean jobEnabled;
    String jobCron;
    String[] consumerGroups;
    String[] producerGroups;
    boolean concurrent_scheduler;
    String scheduler_name;


    @Activate
    @Modified
    protected void activate(WorkfrontUserSyncConfig config) {
        this.enabled = config.enabled();
        this.jobEnabled = config.jobEnabled();
        jobCron = config.jobCron();
        consumerGroups = config.consumerGroups();
        producerGroups = config.producerGroups();
        concurrent_scheduler = config.concurrent_scheduler();
        scheduler_name= config.scheduler_name();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isJobEnabled() {
        return jobEnabled;
    }

    public String getJobCron() {
        return jobCron;
    }

    public String[] getConsumerGroups() {
        return consumerGroups;
    }

    public String[] getProducerGroups() {
        return producerGroups;
    }

    public boolean isConcurrent_scheduler() {
        return concurrent_scheduler;
    }

    public String getScheduler_name() {
        return scheduler_name;
    }
}
