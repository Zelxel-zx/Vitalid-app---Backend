-- Align the doctor table with the JPA model and profile API.
UPDATE doctors SET status = 'OFFLINE' WHERE status IS NULL;
UPDATE doctors SET unread_messages = 0 WHERE unread_messages IS NULL;
UPDATE doctors SET verified = FALSE WHERE verified IS NULL;
UPDATE doctors SET experience_years = 0 WHERE experience_years IS NULL;

ALTER TABLE doctors ALTER COLUMN status SET DEFAULT 'OFFLINE';
ALTER TABLE doctors ALTER COLUMN status SET NOT NULL;
ALTER TABLE doctors ALTER COLUMN unread_messages SET DEFAULT 0;
ALTER TABLE doctors ALTER COLUMN unread_messages SET NOT NULL;
ALTER TABLE doctors ALTER COLUMN verified SET DEFAULT FALSE;
ALTER TABLE doctors ALTER COLUMN verified SET NOT NULL;
ALTER TABLE doctors ALTER COLUMN experience_years SET DEFAULT 0;
ALTER TABLE doctors ALTER COLUMN experience_years SET NOT NULL;

ALTER TABLE doctors
    ADD CONSTRAINT chk_doctors_experience_years
    CHECK (experience_years >= 0);

ALTER TABLE doctors
    ADD CONSTRAINT chk_doctors_availability
    CHECK (
        (availability_start IS NULL AND availability_end IS NULL)
        OR (
            availability_start IS NOT NULL
            AND availability_end IS NOT NULL
            AND availability_end > availability_start
        )
    );
