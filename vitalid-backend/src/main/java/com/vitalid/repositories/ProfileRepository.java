package com.vitalid.repositories;

import com.vitalid.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Profile Repository
 * Data access for User profile (reuses User entity)
 * 
 * TODO: Implement query methods:
 * - findByIdWithDetails(Integer id)
 * - updateProfile(Integer id, ...)
 * Or use inherited methods from UserRepository
 */
@Repository
public interface ProfileRepository extends JpaRepository<User, Integer> {

    // TODO: Add custom query methods if needed

}



