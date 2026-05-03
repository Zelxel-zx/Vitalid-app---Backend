package com.vitalid.security;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT Authentication Filter
 * Intercepts requests to validate JWT tokens
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // TODO: Implement JWT authentication filter
    // - Extract JWT token from Authorization header
    // - Validate token using JwtTokenProvider
    // - Set authentication in SecurityContext
    // - Handle invalid tokens

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // TODO: Implement filter logic
        filterChain.doFilter(request, response);
    }

}
