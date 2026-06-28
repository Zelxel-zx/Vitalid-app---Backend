ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_completed BOOLEAN NOT NULL DEFAULT FALSE;
-- Mark existing users as completed if they have a patient or doctor record
UPDATE users u SET profile_completed = TRUE
WHERE EXISTS (SELECT 1 FROM patients p WHERE p.user_id = u.id)
   OR EXISTS (SELECT 1 FROM doctors d WHERE d.user_id = u.id AND d.specialty != 'General');
