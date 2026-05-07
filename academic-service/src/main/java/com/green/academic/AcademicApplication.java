package com.green.academic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EntityScan(basePackages = {"com.green.academic", "com.green.common"})
@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.green.common", "com.green.academic"})
public class AcademicApplication {
    public static void main(String[] args) {
        SpringApplication.run(AcademicApplication.class, args);
    }
}