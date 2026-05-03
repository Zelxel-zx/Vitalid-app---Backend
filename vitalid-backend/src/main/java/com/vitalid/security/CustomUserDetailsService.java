package com.vitalid.security;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom User Details Service
 * Loads user details from database for authentication
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    // TODO: Implement custom user details service
    // - Load user by username/email from database
    // - Return UserDetails with authorities/roles
    // - Handle user not found exception

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO: Implement user loading logic
        throw new UsernameNotFoundException("User not found");
    }

}
