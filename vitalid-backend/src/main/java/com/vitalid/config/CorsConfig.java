package com.vitalid.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS Configuration for Vitalid Backend
 * Configure allowed origins, methods, and headers for cross-origin requests
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // TODO: Implement CORS configuration
        // - Add allowed origins
        // - Configure allowed methods (GET, POST, PUT, DELETE, etc.)
        // - Set allowed headers
        // - Configure credentials handling
    }

}
