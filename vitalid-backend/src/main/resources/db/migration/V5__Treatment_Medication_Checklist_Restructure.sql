-- Vitalid Database - Treatment, medication, and checklist restructuring
-- Version: V5.0.0
-- Description: Adds the relationships needed for treatment-based medication checklists.

-- Treatments become the doctor's prescription container for a patient.
ALTER TABLE treatments ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE treatments ADD COLUMN IF NOT EXISTS start_date DATE;
ALTER TABLE treatments ADD COLUMN IF NOT EXISTS end_date DATE;

-- Existing columns kept temporarily for backward compatibility:
-- treatments.progress is updated from checklist adherence.
-- treatments.next_appointment belongs to appointments.
-- treatments.medications is replaced by medications.treatment_id.

-- Medications now belong to a treatment.
ALTER TABLE medications ADD COLUMN IF NOT EXISTS treatment_id BIGINT;
ALTER TABLE medications ADD COLUMN IF NOT EXISTS instructions TEXT;
ALTER TABLE medications ADD COLUMN IF NOT EXISTS unit_type VARCHAR(50) DEFAULT 'PILL';
ALTER TABLE medications ADD COLUMN IF NOT EXISTS total_pills INTEGER;
ALTER TABLE medications ADD COLUMN IF NOT EXISTS pills_remaining INTEGER;
ALTER TABLE medications ADD COLUMN IF NOT EXISTS low_stock_threshold INTEGER DEFAULT 5;
ALTER TABLE medications ADD COLUMN IF NOT EXISTS side_effects VARCHAR(1000);

-- Prefer an exact medication-name match from the legacy comma-separated column.
WITH exact_matches AS (
    SELECT m.id AS medication_id, MIN(t.id) AS treatment_id
    FROM medications m
    JOIN treatments t
      ON t.patient_id = m.patient_id
     AND t.doctor_id = m.doctor_id
    CROSS JOIN LATERAL regexp_split_to_table(COALESCE(t.medications, ''), ',') legacy_name
    WHERE m.treatment_id IS NULL
      AND LOWER(TRIM(legacy_name)) = LOWER(TRIM(m.name))
    GROUP BY m.id
    HAVING COUNT(DISTINCT t.id) = 1
)
UPDATE medications m
SET treatment_id = exact_matches.treatment_id
FROM exact_matches
WHERE m.id = exact_matches.medication_id;

-- If the legacy text does not identify it, use the only treatment for that patient/doctor.
WITH single_treatment AS (
    SELECT m.id AS medication_id, MIN(t.id) AS treatment_id
    FROM medications m
    JOIN treatments t
      ON t.patient_id = m.patient_id
     AND t.doctor_id = m.doctor_id
    WHERE m.treatment_id IS NULL
    GROUP BY m.id
    HAVING COUNT(DISTINCT t.id) = 1
)
UPDATE medications m
SET treatment_id = single_treatment.treatment_id
FROM single_treatment
WHERE m.id = single_treatment.medication_id;

-- Do not silently leave or guess ambiguous medication relationships.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM medications WHERE treatment_id IS NULL) THEN
        RAISE EXCEPTION
            'V5 migration cannot assign every medication to one treatment. Resolve ambiguous or missing treatment relationships first.';
    END IF;
END $$;

ALTER TABLE medications
    ADD CONSTRAINT fk_medications_treatment
    FOREIGN KEY (treatment_id) REFERENCES treatments(id) ON DELETE CASCADE;
ALTER TABLE medications ALTER COLUMN treatment_id SET NOT NULL;

-- Checklists should represent the visual checklist for a treatment.
ALTER TABLE checklists ADD COLUMN IF NOT EXISTS treatment_id BIGINT;
ALTER TABLE checklists ALTER COLUMN medication_id DROP NOT NULL;

UPDATE checklists c
SET treatment_id = m.treatment_id
FROM medications m
WHERE c.treatment_id IS NULL
  AND c.medication_id = m.id;

ALTER TABLE checklists
    ADD CONSTRAINT fk_checklists_treatment
    FOREIGN KEY (treatment_id) REFERENCES treatments(id) ON DELETE CASCADE;

-- Scheduled times can now be tied directly to a medication.
ALTER TABLE scheduled_times ADD COLUMN IF NOT EXISTS medication_id BIGINT;
ALTER TABLE scheduled_times ALTER COLUMN checklist_id DROP NOT NULL;

UPDATE scheduled_times st
SET medication_id = c.medication_id
FROM checklists c
WHERE st.medication_id IS NULL
  AND st.checklist_id = c.id;

ALTER TABLE scheduled_times
    ADD CONSTRAINT fk_scheduled_times_medication
    FOREIGN KEY (medication_id) REFERENCES medications(id) ON DELETE CASCADE;

ALTER TABLE scheduled_times
    ADD CONSTRAINT chk_scheduled_times_owner
    CHECK (medication_id IS NOT NULL OR checklist_id IS NOT NULL);

-- Dosage records get enough context to audit treatment adherence by date.
ALTER TABLE dosage_records ADD COLUMN IF NOT EXISTS treatment_id BIGINT;
ALTER TABLE dosage_records ADD COLUMN IF NOT EXISTS patient_id BIGINT;
ALTER TABLE dosage_records ADD COLUMN IF NOT EXISTS scheduled_date DATE;

UPDATE dosage_records dr
SET treatment_id = m.treatment_id,
    patient_id = m.patient_id,
    scheduled_date = COALESCE(dr.timestamp::DATE, CURRENT_DATE)
FROM medications m
WHERE dr.medication_id = m.id
  AND (
      dr.treatment_id IS NULL
      OR dr.patient_id IS NULL
      OR dr.scheduled_date IS NULL
  );

ALTER TABLE dosage_records
    ADD CONSTRAINT fk_dosage_records_treatment
    FOREIGN KEY (treatment_id) REFERENCES treatments(id) ON DELETE CASCADE;

ALTER TABLE dosage_records
    ADD CONSTRAINT fk_dosage_records_patient
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE;

-- Patient-entered stock history. This supports low-stock notifications and audits.
CREATE TABLE IF NOT EXISTS medication_stock_events (
    id BIGSERIAL PRIMARY KEY,
    medication_id BIGINT NOT NULL REFERENCES medications(id) ON DELETE CASCADE,
    patient_id BIGINT NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- Keep the two original records useful for API verification whenever the database is created.
UPDATE treatments
SET description = CASE id
        WHEN 1 THEN 'Control diario de presión arterial'
        WHEN 2 THEN 'Control diario de glucosa'
        ELSE description
    END,
    status = 'ACTIVE',
    progress = 2,
    start_date = CURRENT_DATE - 1,
    end_date = CURRENT_DATE + 30
WHERE id IN (1, 2);

UPDATE medications
SET instructions = CASE id
        WHEN 1 THEN 'Tomar con agua y controlar la presión'
        WHEN 2 THEN 'Tomar junto con alimentos'
        ELSE instructions
    END,
    unit_type = 'PILL',
    total_pills = CASE id
        WHEN 1 THEN 30
        WHEN 2 THEN 60
        ELSE total_pills
    END,
    pills_remaining = CASE id
        WHEN 1 THEN 4
        WHEN 2 THEN 18
        ELSE pills_remaining
    END,
    low_stock_threshold = CASE id
        WHEN 1 THEN 5
        WHEN 2 THEN 10
        ELSE low_stock_threshold
    END,
    side_effects = CASE id
        WHEN 1 THEN 'Mareo leve'
        WHEN 2 THEN ''
        ELSE side_effects
    END,
    start_date = CURRENT_DATE - 1,
    end_date = CURRENT_DATE + 30
WHERE id IN (1, 2);

-- Reuse the two legacy dosage rows as today's taken-dose examples.
UPDATE dosage_records dr
SET treatment_id = m.treatment_id,
    patient_id = m.patient_id,
    scheduled_date = CURRENT_DATE,
    scheduled_time = CASE dr.medication_id
        WHEN 1 THEN '07:00'
        WHEN 2 THEN '08:00'
        ELSE dr.scheduled_time
    END,
    actual_time = CASE dr.medication_id
        WHEN 1 THEN '07:05'
        WHEN 2 THEN '08:10'
        ELSE dr.actual_time
    END,
    is_taken = TRUE,
    timestamp = CASE dr.medication_id
        WHEN 1 THEN CURRENT_DATE + TIME '07:05'
        WHEN 2 THEN CURRENT_DATE + TIME '08:10'
        ELSE dr.timestamp
    END
FROM medications m
WHERE dr.medication_id = m.id
  AND dr.medication_id IN (1, 2);

-- Two auditable purchase events, one for each seeded medication.
INSERT INTO medication_stock_events
    (medication_id, patient_id, quantity, event_type, notes, created_at)
SELECT m.id,
       m.patient_id,
       CASE m.id WHEN 1 THEN 10 WHEN 2 THEN 20 END,
       'PURCHASE',
       'Initial API verification stock',
       NOW()
FROM medications m
WHERE m.id IN (1, 2)
  AND NOT EXISTS (
      SELECT 1
      FROM medication_stock_events mse
      WHERE mse.medication_id = m.id
        AND mse.event_type = 'PURCHASE'
        AND mse.notes = 'Initial API verification stock'
  );

-- Useful indexes for the new API shape.
CREATE INDEX IF NOT EXISTS idx_medications_treatment_id ON medications(treatment_id);
CREATE INDEX IF NOT EXISTS idx_checklists_treatment_id ON checklists(treatment_id);
CREATE INDEX IF NOT EXISTS idx_scheduled_times_medication_id ON scheduled_times(medication_id);
CREATE INDEX IF NOT EXISTS idx_dosage_records_treatment_id ON dosage_records(treatment_id);
CREATE INDEX IF NOT EXISTS idx_dosage_records_patient_id ON dosage_records(patient_id);
CREATE INDEX IF NOT EXISTS idx_dosage_records_medication_date ON dosage_records(medication_id, scheduled_date);
CREATE INDEX IF NOT EXISTS idx_dosage_records_treatment_date ON dosage_records(treatment_id, scheduled_date);
CREATE INDEX IF NOT EXISTS idx_medication_stock_events_medication_id ON medication_stock_events(medication_id);
CREATE INDEX IF NOT EXISTS idx_medication_stock_events_patient_id ON medication_stock_events(patient_id);
