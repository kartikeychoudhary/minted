-- Fix admin user password with correct BCrypt hash for "admin"
UPDATE users
SET password = '$2a$10$MbtJ3USP6CwiE5DGMarrhuT4B7bevHuhCrl0rGwd/dWmYDHc9EKyW'
WHERE username = 'admin';
