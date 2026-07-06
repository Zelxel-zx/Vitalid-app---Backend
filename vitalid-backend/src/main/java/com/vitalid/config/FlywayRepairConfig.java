package com.vitalid.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayRepairConfig {

    private static final Logger logger = LoggerFactory.getLogger(FlywayRepairConfig.class);

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy(
            @Value("${flyway.repair-on-start:${FLYWAY_REPAIR_ON_START:false}}") boolean repairOnStart) {
        return (Flyway flyway) -> {
            if (repairOnStart) {
                logger.warn("FLYWAY_REPAIR_ON_START is enabled. Running flyway.repair() before migrate().");
                flyway.repair();
            }
            flyway.migrate();
        };
    }
}
