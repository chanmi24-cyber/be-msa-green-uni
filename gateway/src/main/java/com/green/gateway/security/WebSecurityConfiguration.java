package com.green.gateway.security;

import com.green.common.model.EnumMemberRole;
import com.green.gateway.filter.TokenAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.sessionManagement( session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) ) //시큐리티에서 session사용 않겠다.
                .httpBasic( hb -> hb.disable() )  //시큐리티에서 제공해주는 로그인 화면이 있는데 사용하지 않겠다
                .formLogin( fl -> fl.disable() ) //어차피 BE가 화면을 만들지 않기 때문에 formLogin기능도 비활성화하겠다.
                .logout( logout -> logout.disable() )
                .csrf( csrf -> csrf.disable() ) //어차피 BE가 화면을 만들지 않으면 csrf 공격이 의미가 없기 때문에 비활성화하겠다.
                .cors( cors -> cors.configurationSource(corsConfigurationSource()) )
                //인가처리 (권한처리)
                .authorizeHttpRequests(auth -> auth// 1. 인증이 아예 필요 없는 화이트리스트 (로그인, 재발급 등)
                        .requestMatchers("/api/auth/login", "/api/auth/reissue").permitAll()

                        // 2. 권한별 접근 제어 (예시: 관리자 전용 경로)
                        .requestMatchers("/api/admin/**").hasRole(EnumMemberRole.ADMIN.name())

                        // 3. 인증된 사용자만 접근 가능한 경로 (로그인이 필요한 일반 기능들)
                        .requestMatchers("/api/member/**").authenticated()

                        // 4. 그 외 모든 요청은 허용 (필요에 따라 .authenticated()로 변경 가능)
                        .anyRequest().permitAll()
                )

                //아래 내용은 (POST) /api/board 로 요청이 올 때는 반드시 로그인이 되어있어야 한다.
                .exceptionHandling(e -> e.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // 3. CORS 상세 설정 Bean
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins( List.of("http://localhost:5173", "http://localhost:5174") );
        config.setAllowedMethods( List.of("GET", "POST", "PUT", "DELETE", "OPTIONS") );
        config.setAllowedHeaders( List.of("*") );
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}