-- Add file_type column to credit_card_statements
ALTER TABLE credit_card_statements ADD COLUMN file_type VARCHAR(10) DEFAULT 'PDF';
