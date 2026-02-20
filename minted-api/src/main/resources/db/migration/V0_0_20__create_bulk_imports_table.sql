CREATE TABLE bulk_imports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    import_type VARCHAR(30) NOT NULL DEFAULT 'CSV',
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT DEFAULT 0,
    total_rows INT NOT NULL DEFAULT 0,
    valid_rows INT NOT NULL DEFAULT 0,
    duplicate_rows INT NOT NULL DEFAULT 0,
    error_rows INT NOT NULL DEFAULT 0,
    imported_rows INT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    job_execution_id BIGINT NULL,
    csv_data LONGTEXT NULL,
    validation_result JSON NULL,
    error_message TEXT NULL,
    skip_duplicates BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_import_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_import_account FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_import_job_exec FOREIGN KEY (job_execution_id)
        REFERENCES job_executions(id) ON DELETE SET NULL
);

CREATE INDEX idx_import_user ON bulk_imports(user_id);
CREATE INDEX idx_import_status ON bulk_imports(status);

-- Seed the bulk import job schedule config
INSERT INTO job_schedule_configs (job_name, cron_expression, enabled, description)
VALUES ('BULK_IMPORT_PROCESSOR', '0 */5 * * * ?', TRUE,
        'Processes bulk CSV imports on demand. Validates, deduplicates, and inserts transactions.');
