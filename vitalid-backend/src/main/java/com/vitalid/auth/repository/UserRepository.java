package com.vitalid.auth.repository;

import com.vitalid.auth.entity.User;
import com.vitalid.auth.entity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
/**
 * User Repository
 * Data access for User entity
 * 
 * TODO: Implement query methods:
 * - findByEmail(String email)
 * - findByPhone(String phone)
 * - existsByEmail(String email)
 * - existsByPhone(String phone)
 * - findByType(UserType type)
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByType(UserType type);
}
