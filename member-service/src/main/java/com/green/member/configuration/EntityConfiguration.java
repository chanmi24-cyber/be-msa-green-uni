package com.green.member.configuration;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EntityScan(basePackages = {"com.green.member", "com.green.common"})
public class EntityConfiguration {
}
