-- Vitalid Database - Doctor extensions
-- Version: V4.0.0
-- Description: Add verification, experience, and availability fields for doctors

ALTER TABLE doctors ADD COLUMN IF NOT EXISTS verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE doctors ADD COLUMN IF NOT EXISTS experience_years INTEGER;
ALTER TABLE doctors ADD COLUMN IF NOT EXISTS availability_start TIME;
ALTER TABLE doctors ADD COLUMN IF NOT EXISTS availability_end TIME;
