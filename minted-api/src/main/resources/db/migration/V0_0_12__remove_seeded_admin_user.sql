-- Remove admin user from database
-- Admin user will be created at runtime by DataInitializer with correct BCrypt hash
-- This ensures password encoding matches the application's PasswordEncoder configuration

-- Delete all data associated with admin user first (to avoid foreign key constraints)
-- These cascading deletes should happen automatically due to ON DELETE CASCADE,
-- but we'll be explicit to ensure clean removal

-- Delete transactions for admin user's accounts
DELETE FROM transactions WHERE user_id = 1;

-- Delete budgets for admin user
DELETE FROM budgets WHERE user_id = 1;

-- Delete dashboard cards for admin user
DELETE FROM dashboard_cards WHERE user_id = 1;

-- Delete accounts for admin user (this should cascade automatically, but being explicit)
DELETE FROM accounts WHERE user_id = 1;

-- Finally, delete the admin user
DELETE FROM users WHERE username = 'admin';
