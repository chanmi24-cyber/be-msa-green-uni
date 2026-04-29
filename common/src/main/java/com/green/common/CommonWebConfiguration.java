package com.green.common;

import com.green.common.auth.MemberContextInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CommonWebConfiguration implements WebMvcConfigurer {
    private final MemberContextInterceptor memberContextInterceptor;
    private final String apiPrefix;

    public CommonWebConfiguration(MemberContextInterceptor memberContextInterceptor
            , @Value("${constants.api.prefix:/api}") String apiPrefix) {
        this.memberContextInterceptor = memberContextInterceptor;
        log.info("============= apiPrefix: {}", apiPrefix);
        this.apiPrefix = apiPrefix;
    }
//
//    @Override
//    public void configurePathMatch(PathMatchConfigurer configurer) {
//        if (StringUtils.hasText(apiPrefix)) {
//            // 모든 RestController에 프로퍼티로 받은 prefix를 강제 적용
//            configurer.addPathPrefix(apiPrefix, HandlerTypePredicate.forAnnotation(RestController.class));
//        }
//    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(memberContextInterceptor)
                .addPathPatterns("/**"); // 모든 경로에서 인터셉터 작동;
    }
}