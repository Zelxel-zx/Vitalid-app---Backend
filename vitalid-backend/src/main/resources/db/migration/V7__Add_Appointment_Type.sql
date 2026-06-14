-- Adds the appointment modality selected by the patient.
ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS appointment_type VARCHAR(20) NOT NULL DEFAULT 'IN_PERSON';

ALTER TABLE appointments
    ADD CONSTRAINT chk_appointments_type
    CHECK (appointment_type IN ('IN_PERSON', 'VIDEO_CALL'));

CREATE INDEX IF NOT EXISTS idx_appointments_type ON appointments(appointment_type);
