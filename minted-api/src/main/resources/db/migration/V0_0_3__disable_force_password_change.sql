-- Disable force password change for admin user to allow testing
UPDATE users
SET force_password_change = false
WHERE username = 'admin';
