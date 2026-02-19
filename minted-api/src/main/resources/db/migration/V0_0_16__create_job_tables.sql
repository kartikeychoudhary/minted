-- ============================================================
-- Job Scheduling & Execution Tracking Tables
-- ============================================================

-- Stores the schedule configuration for each job type
CREATE TABLE job_schedule_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL UNIQUE,
    cron_expression VARCHAR(50) NOT NULL DEFAULT '0 0 1 * * ?',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    last_run_at DATETIME NULL,
    description VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Stores each execution of a job (one row per run)
CREATE TABLE job_executions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL,
    schedule_config_id BIGINT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'RUNNING',
    trigger_type VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    start_time DATETIME NOT NULL,
    end_time DATETIME NULL,
    error_message TEXT NULL,
    total_steps INT NOT NULL DEFAULT 0,
    completed_steps INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_job_exec_schedule FOREIGN KEY (schedule_config_id)
        REFERENCES job_schedule_configs(id) ON DELETE SET NULL
);

-- Stores each step within a job execution
CREATE TABLE job_step_executions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_execution_id BIGINT NOT NULL,
    step_name VARCHAR(200) NOT NULL,
    step_order INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    context_json TEXT NULL,
    error_message TEXT NULL,
    start_time DATETIME NULL,
    end_time DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_step_exec_job FOREIGN KEY (job_execution_id)
        REFERENCES job_executions(id) ON DELETE CASCADE
);

-- Index for querying job executions by name and status
CREATE INDEX idx_job_exec_name ON job_executions(job_name);
CREATE INDEX idx_job_exec_status ON job_executions(status);
CREATE INDEX idx_job_step_exec_job_id ON job_step_executions(job_execution_id);

-- Seed the recurring transaction processor schedule
INSERT INTO job_schedule_configs (job_name, cron_expression, enabled, description)
VALUES ('RECURRING_TRANSACTION_PROCESSOR', '0 0 1 * * ?', TRUE,
        'Processes all active recurring transactions daily. Creates transactions for any that are due today.');
