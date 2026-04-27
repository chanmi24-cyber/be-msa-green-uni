package com.green.common;

import com.green.common.constants.ConstJwt;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationPropertiesScan("com.green.common")
@EnableConfigurationProperties(ConstJwt.class)
public class CommonConfiguration { }