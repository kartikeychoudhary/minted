package com.minted.api.service.impl;

import com.minted.api.service.JobSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobSchedulerServiceImpl implements JobSchedulerService {

    private final TaskScheduler taskScheduler;
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final Map<String, Runnable> registeredTasks = new ConcurrentHashMap<>();

    @Override
    public void registerJob(String jobName, Runnable task, String cronExpression, boolean enabled) {
        log.info("Registering job {} with cron {} (Enabled: {})", jobName, cronExpression, enabled);
        registeredTasks.put(jobName, task);

        if (enabled) {
            scheduleTask(jobName, task, cronExpression);
        }
    }

    @Override
    public void rescheduleJob(String jobName, String cronExpression, boolean enabled) {
        log.info("Rescheduling job {} to cron {} (Enabled: {})", jobName, cronExpression, enabled);
        
        // Cancel existing schedule if any
        ScheduledFuture<?> existingTask = scheduledTasks.get(jobName);
        if (existingTask != null) {
            existingTask.cancel(false);
            scheduledTasks.remove(jobName);
            log.debug("Cancelled existing schedule for job: {}", jobName);
        }

        // Schedule anew if enabled
        if (enabled) {
            Runnable task = registeredTasks.get(jobName);
            if (task != null) {
                scheduleTask(jobName, task, cronExpression);
            } else {
                log.warn("Cannot reschedule job {}: No runnable task registered", jobName);
            }
        }
    }

    @Override
    public void triggerJob(String jobName) {
        Runnable task = registeredTasks.get(jobName);
        if (task != null) {
            log.info("Manually executing job: {}", jobName);
            // Execute immediately in a different thread to avoid blocking the HTTP request
            new Thread(() -> {
                try {
                    task.run();
                } catch (Exception e) {
                    log.error("Error during manual execution of job {}", jobName, e);
                }
            }, "Manual-Trigger-" + jobName).start();
        } else {
            throw new IllegalArgumentException("No registered task for job: " + jobName);
        }
    }

    private void scheduleTask(String jobName, Runnable task, String cronExpression) {
        try {
            ScheduledFuture<?> scheduledTask = taskScheduler.schedule(task, new CronTrigger(cronExpression));
            scheduledTasks.put(jobName, scheduledTask);
            log.info("Successfully scheduled job {}", jobName);
        } catch (IllegalArgumentException e) {
            log.error("Invalid cron expression for job {}: {}", jobName, cronExpression, e);
        }
    }
}
