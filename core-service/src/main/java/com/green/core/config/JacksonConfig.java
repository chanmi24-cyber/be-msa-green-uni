package com.green.core.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    // JS Number 최대 안전 정수(2^53)를 초과하는 Java Long ID의 정밀도 손실 방지
    // Spring Boot가 Module Bean을 자동으로 ObjectMapper에 등록함
    @Bean
    public Module longToStringModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        return module;
    }
}
