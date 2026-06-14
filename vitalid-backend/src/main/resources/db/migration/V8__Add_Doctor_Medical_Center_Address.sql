-- Adds the address where the doctor provides in-person care.
ALTER TABLE doctors
    ADD COLUMN IF NOT EXISTS medical_center_address VARCHAR(500);
