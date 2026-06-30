UPDATE users u
SET profile_completed = TRUE
WHERE profile_completed = FALSE
  AND (
      EXISTS (
          SELECT 1
          FROM patients p
          WHERE p.user_id = u.id
      )
      OR EXISTS (
          SELECT 1
          FROM doctors d
          WHERE d.user_id = u.id
            AND d.medical_center_address IS NOT NULL
            AND TRIM(d.medical_center_address) <> ''
            AND d.availability_start IS NOT NULL
            AND d.availability_end IS NOT NULL
      )
  );
