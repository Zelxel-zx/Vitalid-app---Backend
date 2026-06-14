-- Vitalid Database Initial Schema
-- Version: V1.0.0
-- Description: Initial database structure for Vitalid telemedicine platform

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(200) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE TABLE doctors (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    specialty VARCHAR(100) NOT NULL,
    avatar VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'OFFLINE',
    unread_messages INTEGER NOT NULL DEFAULT 0,
    verified BOOLEAN DEFAULT FALSE,
    experience_years INTEGER,
    availability_start TIME,
    availability_end TIME,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE TABLE patients (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    date_of_birth DATE,
    blood_type VARCHAR(10),
    phone_number VARCHAR(50),
    address VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    zip_code VARCHAR(20),
    medical_history TEXT,
    allergies TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE TABLE appointments (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    doctor_id BIGINT NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    time TIME NOT NULL,
    reason VARCHAR(255),
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    sent_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE treatments (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    doctor_id BIGINT NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    progress INTEGER NOT NULL DEFAULT 0,
    next_appointment DATE,
    medications TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE TABLE medications (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    doctor_id BIGINT NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    dosage VARCHAR(100),
    frequency VARCHAR(50),
    prescribed_by VARCHAR(150),
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE TABLE checklists (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    medication_id BIGINT NOT NULL REFERENCES medications(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE TABLE scheduled_times (
    id BIGSERIAL PRIMARY KEY,
    checklist_id BIGINT NOT NULL REFERENCES checklists(id) ON DELETE CASCADE,
    time VARCHAR(10) NOT NULL
);

CREATE TABLE dosage_records (
    id BIGSERIAL PRIMARY KEY,
    checklist_id BIGINT NOT NULL REFERENCES checklists(id) ON DELETE CASCADE,
    medication_id BIGINT NOT NULL REFERENCES medications(id) ON DELETE CASCADE,
    scheduled_time VARCHAR(10),
    actual_time VARCHAR(20),
    is_taken BOOLEAN NOT NULL DEFAULT FALSE,
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE health_metrics (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    metric VARCHAR(100) NOT NULL,
    value DOUBLE PRECISION NOT NULL,
    unit VARCHAR(20),
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    notes TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

INSERT INTO users (name, email, password, phone, type)
VALUES
    ('Ana Gómez', 'ana.gomez@vitalid.com', 'password123', '+5491122334455', 'DOCTOR'),
    ('Luis Torres', 'luis.torres@vitalid.com', 'password123', '+5491166655544', 'DOCTOR'),
    ('Marta Rojas', 'marta.rojas@vitalid.com', 'password123', '+5491177766655', 'DOCTOR'),
    ('Paola Sánchez', 'paola.sanchez@vitalid.com', 'password123', '+5491188877766', 'PATIENT'),
    ('Carlos Méndez', 'carlos.mendez@vitalid.com', 'password123', '+5491199988877', 'PATIENT');

INSERT INTO doctors (user_id, specialty, avatar, status, unread_messages)
VALUES
    (1, 'Cardiología', 'https://example.com/avatars/ana.jpg', 'ONLINE', 2),
    (2, 'Dermatología', 'https://example.com/avatars/luis.jpg', 'OFFLINE', 1),
    (3, 'Pediatría', 'https://example.com/avatars/marta.jpg', 'BUSY', 0);

INSERT INTO patients (user_id, date_of_birth, blood_type, phone_number, address, city, state, zip_code, medical_history, allergies)
VALUES
    (4, '1987-05-21', 'A+', '+5491188877766', 'Av. Santa Fe 1234', 'Buenos Aires', 'Buenos Aires', 'C1000', 'Hipertensión leve', 'Alergia a la penicilina'),
    (5, '1990-12-03', 'O-', '+5491199988877', 'Calle Falsa 742', 'Rosario', 'Santa Fe', 'S2000', 'Diabetes tipo 2', 'Sin alergias conocidas');

INSERT INTO appointments (patient_id, doctor_id, date, time, reason, status)
VALUES
    (1, 1, '2026-05-10', '10:30', 'Control de presión arterial', 'SCHEDULED'),
    (2, 2, '2026-05-12', '14:00', 'Revisión de piel', 'SCHEDULED');

INSERT INTO medications (patient_id, doctor_id, name, dosage, frequency, prescribed_by, start_date, end_date)
VALUES
    (1, 1, 'Losartán', '50 mg', 'DIARIO', 'Dr. Ana Gómez', '2026-05-01', '2026-06-01'),
    (2, 2, 'Metformina', '500 mg', 'DIARIO', 'Dr. Luis Torres', '2026-04-20', '2026-10-20');

INSERT INTO treatments (patient_id, doctor_id, title, status, progress, next_appointment, medications)
VALUES
    (1, 1, 'Tratamiento de hipertensión', 'ACTIVE', 40, '2026-07-24', 'Losartán'),
    (2, 2, 'Control de diabetes', 'COMPLETED', 100, '2026-05-14', 'Metformina');

INSERT INTO checklists (patient_id, medication_id)
VALUES
    (1, 1),
    (2, 2);

INSERT INTO scheduled_times (checklist_id, time)
VALUES
    (1, '07:00'),
    (1, '21:00'),
    (2, '08:00'),
    (2, '20:00');

INSERT INTO dosage_records (checklist_id, medication_id, scheduled_time, actual_time, is_taken)
VALUES
    (1, 1, '07:00', '2026-05-05T07:05:00', true),
    (2, 2, '08:00', '2026-05-05T08:10:00', true);

INSERT INTO health_metrics (patient_id, metric, value, unit, timestamp, notes)
VALUES
    (1, 'PRESSURE', 128.0, 'mmHg', '2026-05-05T09:00:00', 'Presión arterial estable'),
    (2, 'GLUCOSE', 105.0, 'mg/dL', '2026-05-05T08:30:00', 'Control de glucosa matutino');
