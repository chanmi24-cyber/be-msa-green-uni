package com.green.common.outbox;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;


public class OutboxPackageRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 기존 스캔 목록을 유지하면서 "com.green.eats.common.outbox" 패키지만 추가로 등록합니다.
        AutoConfigurationPackages.register(registry, "com.green.common.outbox");
    }
}