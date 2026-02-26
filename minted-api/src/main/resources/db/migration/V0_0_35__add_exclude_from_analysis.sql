-- Add exclude_from_analysis flag to transactions
ALTER TABLE transactions ADD COLUMN exclude_from_analysis BOOLEAN DEFAULT FALSE;
