ALTER TABLE medications DROP COLUMN IF EXISTS total_pills;

UPDATE doctors
SET experience_years = CASE id
    WHEN 1 THEN 8
    WHEN 2 THEN 6
    WHEN 3 THEN 10
    ELSE experience_years
END
WHERE id IN (1, 2, 3);

UPDATE doctors
SET availability_start = COALESCE(availability_start, TIME '09:00'),
    availability_end = COALESCE(availability_end, TIME '17:00'),
    medical_center_address = COALESCE(
        NULLIF(TRIM(medical_center_address), ''),
        CASE id
            WHEN 1 THEN 'Av. Javier Prado Este 4200, Santiago de Surco'
            WHEN 2 THEN 'Av. Arequipa 2450, Lince'
            WHEN 3 THEN 'Av. Brasil 2730, Pueblo Libre'
            ELSE 'Centro medico Vitalid'
        END
    );
