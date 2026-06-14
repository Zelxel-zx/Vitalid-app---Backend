-- Enable the original demo accounts for frontend testing.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Hash only legacy seed passwords that are still stored as plain text.
UPDATE users
SET password = crypt('password123', gen_salt('bf', 10))
WHERE email IN (
    'ana.gomez@vitalid.com',
    'luis.torres@vitalid.com',
    'marta.rojas@vitalid.com',
    'paola.sanchez@vitalid.com',
    'carlos.mendez@vitalid.com'
)
AND password = 'password123';

-- Verify only the three doctors created by the initial seed.
UPDATE doctors d
SET verified = TRUE
FROM users u
WHERE d.user_id = u.id
  AND u.email IN (
      'ana.gomez@vitalid.com',
      'luis.torres@vitalid.com',
      'marta.rojas@vitalid.com'
  );
