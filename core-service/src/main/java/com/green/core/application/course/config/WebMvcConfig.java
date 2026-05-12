package com.green.core.application.course.config;

import com.green.common.auth.MemberContextInterceptor;
import com.green.core.application.course.StudentStatusInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final MemberContextInterceptor memberContextInterceptor;
    private final StudentStatusInterceptor studentStatusInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(memberContextInterceptor)  // 먼저 실행
                .addPathPatterns("/**");

        registry.addInterceptor(studentStatusInterceptor)
                .addPathPatterns("/api/core/student/courses", "/api/core/student/courses/**")
                .excludePathPatterns("/api/core/student/courses/status"); // 상태 조회는 제외
    }
}