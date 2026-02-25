-- Remove the BULK_IMPORT_PROCESSOR scheduled job config.
-- Bulk import processing is user-action driven (triggered on confirm),
-- so the recurring 5-minute cron sweep is unnecessary.
DELETE FROM job_schedule_configs WHERE job_name = 'BULK_IMPORT_PROCESSOR';
