package com.green.member.configuration;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableFeignClients
@EnableScheduling
@EntityScan(basePackages = {"com.green.member", "com.green.common"})
@EnableJpaAuditing
public class AllConfiguration {
}
