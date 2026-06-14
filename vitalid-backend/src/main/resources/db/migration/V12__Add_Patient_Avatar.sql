ALTER TABLE patients
    ADD COLUMN IF NOT EXISTS avatar VARCHAR(255);

UPDATE doctors
SET avatar = NULL
WHERE avatar LIKE 'https://example.com/%';
