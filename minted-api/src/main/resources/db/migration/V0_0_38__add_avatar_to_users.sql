ALTER TABLE users
  ADD COLUMN avatar_data      LONGBLOB       COMMENT 'Avatar image binary data (max 2MB enforced in application)',
  ADD COLUMN avatar_content_type VARCHAR(50) COMMENT 'Image MIME type (image/jpeg, image/png, image/webp)',
  ADD COLUMN avatar_file_size INT            COMMENT 'Image file size in bytes',
  ADD COLUMN avatar_updated_at TIMESTAMP NULL COMMENT 'Last avatar upload timestamp';
