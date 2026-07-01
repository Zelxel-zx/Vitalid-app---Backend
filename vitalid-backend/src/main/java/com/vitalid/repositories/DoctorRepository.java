package com.vitalid.repositories;

import com.vitalid.models.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Doctor Repository
 * Data access for Doctor entity
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findBySpecialtyIgnoreCase(String specialty);
    List<Doctor> findByStatus(String status);
    List<Doctor> findAllByOrderByIdAsc();
    Doctor findByUser_Id(Long userId);
}



