package com.vitalid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.vitalid")
public class VitalidApplication {

    public static void main(String[] args) {
        SpringApplication.run(VitalidApplication.class, args);
    }

}

