package com.green.common.outbox;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ConditionalOnProperty(name = "outbox.enabled", havingValue = "true")
@EnableScheduling // Relay 스케줄러 활성화
@Import(OutboxPackageRegistrar.class)
public class OutboxAutoConfiguration {
}