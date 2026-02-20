-- Add is_default column to transaction_categories and account_types
ALTER TABLE transaction_categories ADD COLUMN is_default BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE account_types ADD COLUMN is_default BOOLEAN NOT NULL DEFAULT FALSE;

-- Mark existing rows that match default_categories as default
UPDATE transaction_categories tc
  INNER JOIN default_categories dc ON LOWER(tc.name) = LOWER(dc.name)
  SET tc.is_default = TRUE;

-- Mark existing rows that match default_account_types as default
UPDATE account_types at2
  INNER JOIN default_account_types dat ON LOWER(at2.name) = LOWER(dat.name)
  SET at2.is_default = TRUE;
