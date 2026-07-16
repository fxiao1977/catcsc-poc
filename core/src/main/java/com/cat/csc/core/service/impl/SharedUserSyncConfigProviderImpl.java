package com.cat.csc.core.service.impl;

import com.cat.csc.core.schedulers.WorkfrontUserSyncConfig;
import com.cat.csc.core.service.SharedUserSyncConfigProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Modified;


@Component(service = SharedUserSyncConfigProvider.class, immediate = true)
@Designate(ocd = WorkfrontUserSyncConfig.class)
public class SharedUserSyncConfigProviderImpl implements SharedUserSyncConfigProvider {

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

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isJobEnabled() {
        return jobEnabled;
    }

    @Override
    public String getJobCron() {
        return jobCron;
    }

    @Override
    public String[] getConsumerGroups() {
        return consumerGroups;
    }

    @Override
    public String[] getProducerGroups() {
        return producerGroups;
    }

    @Override
    public boolean isConcurrent_scheduler() {
        return concurrent_scheduler;
    }

    @Override
    public String getScheduler_name() {
        return scheduler_name;
    }
}
