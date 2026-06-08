package com.green.core.application.tuition.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.Bean; // 💡 추가
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    // 🎯 @Bean 지정을 통해 'mailAsyncExecutor'라는 이름의 빈을 스프링 컨테이너에 등록합니다.
    @Bean(name = "mailAsyncExecutor")
    public Executor mailAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("MailAsync-");
        executor.initialize();
        return executor;
    }
}