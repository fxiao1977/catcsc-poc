package com.cat.csc.core.service;

import java.util.List;

public interface SharedUserSyncConfigProvider {
    boolean isEnabled();

    boolean isJobEnabled();

    String getJobCron();

    String[] getConsumerGroups();

    String[] getProducerGroups();

    boolean isConcurrent_scheduler();

    String getScheduler_name();
}
