package com.vitalid.doctor.repository;

import com.vitalid.doctor.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Doctor Repository
 * Data access for Doctor entity
 * 
 * TODO: Implement query methods:
 * - findBySpecialty(String specialty)
 * - findByStatus(String status)
 * - findByUserId(Integer userId)
 * - findAllByOrderByNameAsc()
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {

    // TODO: Add custom query methods

}
