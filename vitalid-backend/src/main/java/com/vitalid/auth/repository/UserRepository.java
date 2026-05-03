package com.vitalid.auth.repository;

import com.vitalid.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * User Repository
 * Data access for User entity
 * 
 * TODO: Implement query methods:
 * - findByEmail(String email)
 * - findByPhone(String phone)
 * - existsByEmail(String email)
 * - existsByPhone(String phone)
 * - findByType(String type)
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // TODO: Add custom query methods

}
