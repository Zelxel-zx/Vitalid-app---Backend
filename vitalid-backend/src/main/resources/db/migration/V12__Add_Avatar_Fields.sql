-- V12: Change doctor avatar from VARCHAR(255) to TEXT (to hold base64 data URIs)
-- and add avatar TEXT column to patients table

ALTER TABLE doctors ALTER COLUMN avatar TYPE TEXT;
ALTER TABLE patients ADD COLUMN IF NOT EXISTS avatar TEXT;
