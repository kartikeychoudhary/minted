-- Add role column to users table for admin access control
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';

-- Set the admin user's role to ADMIN
UPDATE users SET role = 'ADMIN' WHERE username = 'admin';
