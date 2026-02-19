package com.minted.api.service;

public interface JobSchedulerService {

    void registerJob(String jobName, Runnable task, String cronExpression, boolean enabled);

    void rescheduleJob(String jobName, String cronExpression, boolean enabled);

    void triggerJob(String jobName);
}
