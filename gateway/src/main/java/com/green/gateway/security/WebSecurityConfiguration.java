package com.green.gateway.security;

import com.green.common.enumcode.EnumMemberRole;
import com.green.gateway.filter.TokenAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

// @Configuration 애노테이션 아래에 있는 @Bean은 무조건 싱글톤이다.
@Configuration
@RequiredArgsConstructor
public class WebSecurityConfiguration {
    private final TokenAuthenticationFilter tokenAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.sessionManagement( session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) ) //시큐리티에서 session 사용X
                .httpBasic( hb -> hb.disable() )  //시큐리티에서 제공해주는 로그인 화면이 있는데 사용 X
                .formLogin( fl -> fl.disable() ) // BE가 화면을 만들지 않기 때문에 formLogin 비활성화
                .logout( logout -> logout.disable() )
                .csrf( csrf -> csrf.disable() ) //BE가 화면을 만들지 않으면 csrf 공격이 의미가 없기 때문에 비활성
                .cors( cors -> cors.configurationSource(corsConfigurationSource()) )
                //인가처리 (권한처리)
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/api/academic/public/**").permitAll() // 비로그인 가능

                        // 권한별 접근 제어
                        .requestMatchers("/api/*/admin/**").hasRole(EnumMemberRole.ADMIN.name())
                        .requestMatchers("/api/*/student/**").hasRole(EnumMemberRole.STUDENT.name())
                        .requestMatchers("/api/*/professor/**").hasRole(EnumMemberRole.PROFESSOR.name())

                        // 인증된 사용자만 접근 가능
                        .requestMatchers("/api/core/**", "/api/academic/**", "/api/member/**").authenticated()

                        // 그 외 모든 요청은 허용
                        .anyRequest().permitAll()
                )

                .exceptionHandling(e -> e
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)   // 401 - 토큰 없음
                        .accessDeniedHandler(jwtAccessDeniedHandler)             // 403 - 권한 없음
                )
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // CORS 상세 설정 Bean
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins( List.of("http://localhost:5173", "http://localhost:5174", "http://192.168.0.31:5173", "https://localhost:5173", "https://localhost:5174", "https://192.168.0.31:5173") );
        config.setAllowedMethods( List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") );
        config.setAllowedHeaders( List.of("*") );
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}