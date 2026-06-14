package com.vitalid.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.util.TimeZone;

@Configuration
public class TimeZoneConfig {

    public static final ZoneId APPLICATION_ZONE = ZoneId.of("America/Lima");

    @PostConstruct
    void configureDefaultTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(APPLICATION_ZONE));
    }
}
