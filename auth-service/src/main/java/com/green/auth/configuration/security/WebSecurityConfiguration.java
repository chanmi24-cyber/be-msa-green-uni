package com.green.auth.configuration.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // @Configuration 애노테이션 아래에 있는 @Bean은 무조건 싱글톤
@RequiredArgsConstructor
public class WebSecurityConfiguration {

    @Bean //메소드 호출로 리턴값 객체를 빈등록
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //시큐리티에서 session사용 안함
                .httpBasic(hb -> hb.disable()) //시큐리티에서 제공해주는 로그인 사용 안함
                .formLogin(fl -> fl.disable()) //BE가 화면을 만들지 않기 때문에 formLogin기능 비활성화
                .csrf(csrf -> csrf.disable()) //BE가 화면을 만들지 않으면 csrf 공격이 의미가 없기 때문에 비활성화
                .authorizeHttpRequests(req -> req.anyRequest().permitAll())
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); //현존 최강의 단방향 암호화.
    }
}