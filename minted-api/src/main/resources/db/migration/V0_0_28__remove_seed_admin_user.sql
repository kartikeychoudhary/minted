-- Remove the hardcoded admin user from V0_0_1.
-- Admin creation is handled at runtime by DataInitializer with BCrypt encoding,
-- which forces a password change on first login.
-- This migration removes the insecure seed row IF it still has the original hash.
DELETE FROM users
WHERE username = 'admin'
  AND password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';
