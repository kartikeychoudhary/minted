package com.minted.api.recurring.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minted.api.recurring.entity.RecurringTransaction;
import com.minted.api.transaction.entity.Transaction;
import com.minted.api.job.entity.JobExecution;
import com.minted.api.job.entity.JobScheduleConfig;
import com.minted.api.job.entity.JobStepExecution;
import com.minted.api.job.enums.JobStatus;
import com.minted.api.job.enums.JobStepStatus;
import com.minted.api.job.enums.JobTriggerType;
import com.minted.api.recurring.enums.RecurringStatus;
import com.minted.api.job.repository.JobExecutionRepository;
import com.minted.api.job.repository.JobScheduleConfigRepository;
import com.minted.api.recurring.repository.RecurringTransactionRepository;
import com.minted.api.transaction.repository.TransactionRepository;
import com.minted.api.job.service.JobSchedulerService;
import com.minted.api.recurring.service.RecurringTransactionServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecurringTransactionJob implements Runnable {

    public static final String JOB_NAME = "RECURRING_TRANSACTION_PROCESSOR";

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final JobExecutionRepository jobExecutionRepository;
    private final JobScheduleConfigRepository scheduleConfigRepository;
    private final JobSchedulerService jobSchedulerService;
    private final ObjectMapper objectMapper;
    private final RecurringTransactionServiceImpl recurringTransactionService; // for next execution date logic

    @PostConstruct
    public void init() {
        scheduleConfigRepository.findByJobName(JOB_NAME).ifPresent(config -> {
            jobSchedulerService.registerJob(JOB_NAME, this, config.getCronExpression(), config.getEnabled());
        });
    }

    @Override
    @Transactional
    public void run() {
        log.info("Starting RecurringTransactionJob execution...");
        
        // 1. Initialize execution record
        JobScheduleConfig config = scheduleConfigRepository.findByJobName(JOB_NAME).orElse(null);
        JobExecution execution = new JobExecution();
        execution.setJobName(JOB_NAME);
        execution.setScheduleConfig(config);
        execution.setStatus(JobStatus.RUNNING);
        // Default to SCHEDULED, MANUAL sets it differently via service usually, but we assume SCHEDULED by default if run by TaskScheduler
        // For accurate tracking, the JobSchedulerService could pass the triggerType, but keeping it simple here
        execution.setTriggerType(JobTriggerType.SCHEDULED); 
        execution.setStartTime(LocalDateTime.now());
        execution.setTotalSteps(3);
        execution = jobExecutionRepository.save(execution);

        try {
            // Step 1: Fetch
            List<RecurringTransaction> dueTransactions = executeStep1Fetch(execution);
            execution.setCompletedSteps(1);
            jobExecutionRepository.save(execution);

            // Step 2: Process
            executeStep2Process(execution, dueTransactions);
            execution.setCompletedSteps(2);
            jobExecutionRepository.save(execution);

            // Step 3: Update Config
            executeStep3UpdateConfig(execution, config);
            execution.setCompletedSteps(3);
            
            // Mark Completed
            execution.setStatus(JobStatus.COMPLETED);
            execution.setEndTime(LocalDateTime.now());
            jobExecutionRepository.save(execution);
            
            log.info("RecurringTransactionJob completed successfully.");
            
        } catch (Exception e) {
            log.error("RecurringTransactionJob failed", e);
            execution.setStatus(JobStatus.FAILED);
            execution.setEndTime(LocalDateTime.now());
            execution.setErrorMessage(e.getMessage());
            jobExecutionRepository.save(execution);
        }
    }

    private List<RecurringTransaction> executeStep1Fetch(JobExecution execution) throws Exception {
        JobStepExecution step = createStep(execution, "Fetch Due Recurring Transactions", 1);
        
        try {
            LocalDate today = LocalDate.now();
            List<RecurringTransaction> dueTransactions = recurringTransactionRepository
                    .findByStatusAndNextExecutionDateLessThanEqual(RecurringStatus.ACTIVE, today);
            
            Map<String, Object> context = new HashMap<>();
            context.put("dueCount", dueTransactions.size());
            context.put("targetDate", today.toString());
            
            completeStep(step, context);
            return dueTransactions;
        } catch (Exception e) {
            failStep(step, e);
            throw e;
        }
    }

    private void executeStep2Process(JobExecution execution, List<RecurringTransaction> dueTransactions) throws Exception {
        JobStepExecution step = createStep(execution, "Process Transactions", 2);
        
        int created = 0;
        int failed = 0;
        List<Long> failedIds = new ArrayList<>();
        
        try {
            for (RecurringTransaction rt : dueTransactions) {
                try {
                    // Create transaction
                    Transaction tx = new Transaction();
                    tx.setAmount(rt.getAmount());
                    tx.setType(rt.getType());
                    tx.setDescription(rt.getName() + " (Auto-generated)");
                    tx.setTransactionDate(rt.getNextExecutionDate());
                    tx.setAccount(rt.getAccount());
                    tx.setCategory(rt.getCategory());
                    tx.setUser(rt.getUser());
                    tx.setIsRecurring(true);
                    
                    transactionRepository.save(tx);
                    
                    // Update next execution date
                    LocalDate nextDate = recurringTransactionService.calculateNextExecutionDate(
                            rt.getNextExecutionDate().plusDays(1), rt.getDayOfMonth());
                    rt.setNextExecutionDate(nextDate);
                    recurringTransactionRepository.save(rt);
                    
                    created++;
                } catch (Exception ex) {
                    log.error("Failed to process recurring transaction ID: {}", rt.getId(), ex);
                    failed++;
                    failedIds.add(rt.getId());
                }
            }
            
            Map<String, Object> context = new HashMap<>();
            context.put("processed", dueTransactions.size());
            context.put("created", created);
            context.put("failed", failed);
            if (!failedIds.isEmpty()) {
                context.put("failedIds", failedIds);
            }
            
            if (failed > 0 && created == 0 && !dueTransactions.isEmpty()) {
                throw new RuntimeException("All transactions failed to process");
            }
            
            completeStep(step, context);
        } catch (Exception e) {
            failStep(step, e);
            throw e;
        }
    }

    private void executeStep3UpdateConfig(JobExecution execution, JobScheduleConfig config) throws Exception {
        JobStepExecution step = createStep(execution, "Update Schedule Configuration", 3);
        
        try {
            if (config != null) {
                config.setLastRunAt(LocalDateTime.now());
                scheduleConfigRepository.save(config);
            }
            
            Map<String, Object> context = new HashMap<>();
            context.put("configUpdated", config != null);
            
            completeStep(step, context);
        } catch (Exception e) {
            failStep(step, e);
            throw e;
        }
    }

    private JobStepExecution createStep(JobExecution jobExecution, String name, int order) {
        JobStepExecution step = new JobStepExecution();
        step.setJobExecution(jobExecution);
        step.setStepName(name);
        step.setStepOrder(order);
        step.setStatus(JobStepStatus.RUNNING);
        step.setStartTime(LocalDateTime.now());
        jobExecution.getSteps().add(step);
        // Note: Casade.ALL on jobExecution.steps means this will be saved
        return step;
    }

    private void completeStep(JobStepExecution step, Map<String, Object> context) throws Exception {
        step.setStatus(JobStepStatus.COMPLETED);
        step.setEndTime(LocalDateTime.now());
        if (context != null) {
            step.setContextJson(objectMapper.writeValueAsString(context));
        }
    }

    private void failStep(JobStepExecution step, Exception e) {
        step.setStatus(JobStepStatus.FAILED);
        step.setEndTime(LocalDateTime.now());
        step.setErrorMessage(e.getMessage());
    }
}
