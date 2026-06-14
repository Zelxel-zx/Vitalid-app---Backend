-- Seed a patient monitored by Ana Gomez with two omitted doses today.
-- Omitted doses are inferred from elapsed scheduled times without a taken record.

INSERT INTO users (name, email, password, phone, type)
SELECT
    'Miguel Herrera',
    'miguel.herrera@vitalid.com',
    crypt('password123', gen_salt('bf', 10)),
    '+51987654321',
    'PATIENT'
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'miguel.herrera@vitalid.com'
);

INSERT INTO patients (
    user_id,
    date_of_birth,
    blood_type,
    address,
    city,
    state,
    zip_code,
    medical_history,
    allergies,
    is_active
)
SELECT
    u.id,
    DATE '1972-09-18',
    'O+',
    'Av. Primavera 1250',
    'Lima',
    'Lima',
    '15023',
    'Hipertension arterial y colesterol elevado',
    'Sin alergias conocidas',
    TRUE
FROM users u
WHERE u.email = 'miguel.herrera@vitalid.com'
  AND NOT EXISTS (
      SELECT 1
      FROM patients p
      WHERE p.user_id = u.id
  );

INSERT INTO treatments (
    patient_id,
    doctor_id,
    title,
    description,
    status,
    progress,
    start_date,
    end_date,
    next_appointment
)
SELECT
    p.id,
    d.id,
    'Control cardiovascular intensivo',
    'Seguimiento de presion arterial y colesterol',
    'ACTIVE',
    0,
    CURRENT_DATE - 7,
    CURRENT_DATE + 30,
    CURRENT_DATE + 7
FROM patients p
JOIN users patient_user ON patient_user.id = p.user_id
JOIN doctors d ON TRUE
JOIN users doctor_user ON doctor_user.id = d.user_id
WHERE patient_user.email = 'miguel.herrera@vitalid.com'
  AND doctor_user.email = 'ana.gomez@vitalid.com'
  AND NOT EXISTS (
      SELECT 1
      FROM treatments t
      WHERE t.patient_id = p.id
        AND t.doctor_id = d.id
        AND t.title = 'Control cardiovascular intensivo'
  );

INSERT INTO medications (
    treatment_id,
    patient_id,
    doctor_id,
    name,
    dosage,
    frequency,
    prescribed_by,
    start_date,
    end_date,
    instructions,
    unit_type,
    pills_remaining,
    low_stock_threshold,
    side_effects
)
SELECT
    t.id,
    t.patient_id,
    t.doctor_id,
    'Enalapril',
    '10 mg',
    'DIARIO',
    'Dra. Ana Gomez',
    t.start_date,
    t.end_date,
    'Tomar con agua y registrar la presion arterial',
    'PILL',
    20,
    5,
    ''
FROM treatments t
JOIN patients p ON p.id = t.patient_id
JOIN users u ON u.id = p.user_id
WHERE u.email = 'miguel.herrera@vitalid.com'
  AND t.title = 'Control cardiovascular intensivo'
  AND NOT EXISTS (
      SELECT 1
      FROM medications m
      WHERE m.treatment_id = t.id
        AND m.name = 'Enalapril'
  );

INSERT INTO checklists (patient_id, medication_id, treatment_id)
SELECT
    t.patient_id,
    NULL,
    t.id
FROM treatments t
JOIN patients p ON p.id = t.patient_id
JOIN users u ON u.id = p.user_id
WHERE u.email = 'miguel.herrera@vitalid.com'
  AND t.title = 'Control cardiovascular intensivo'
  AND NOT EXISTS (
      SELECT 1
      FROM checklists c
      WHERE c.treatment_id = t.id
  );

INSERT INTO scheduled_times (checklist_id, medication_id, time)
SELECT
    NULL,
    m.id,
    seed_time.time
FROM medications m
JOIN patients p ON p.id = m.patient_id
JOIN users u ON u.id = p.user_id
CROSS JOIN (VALUES ('00:01'), ('00:02')) AS seed_time(time)
WHERE u.email = 'miguel.herrera@vitalid.com'
  AND m.name = 'Enalapril'
  AND NOT EXISTS (
      SELECT 1
      FROM scheduled_times st
      WHERE st.medication_id = m.id
        AND st.time = seed_time.time
  );

-- Paola should have no omitted doses today. Mark every elapsed or future
-- scheduled dose for today as taken, without creating duplicate records.
INSERT INTO dosage_records (
    checklist_id,
    medication_id,
    treatment_id,
    patient_id,
    scheduled_date,
    scheduled_time,
    actual_time,
    is_taken,
    timestamp
)
SELECT
    c.id,
    m.id,
    m.treatment_id,
    m.patient_id,
    CURRENT_DATE,
    st.time,
    st.time,
    TRUE,
    CURRENT_DATE + st.time::TIME
FROM medications m
JOIN patients p ON p.id = m.patient_id
JOIN users u ON u.id = p.user_id
JOIN scheduled_times st ON st.medication_id = m.id
JOIN checklists c ON c.treatment_id = m.treatment_id
WHERE u.email = 'paola.sanchez@vitalid.com'
  AND NOT EXISTS (
      SELECT 1
      FROM dosage_records dr
      WHERE dr.medication_id = m.id
        AND dr.scheduled_date = CURRENT_DATE
        AND dr.scheduled_time = st.time
        AND dr.is_taken = TRUE
  );
