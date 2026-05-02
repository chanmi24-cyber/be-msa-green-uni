package com.green.gateway.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

// Security 필터는 GlobalExceptionHandler가 직접 잡지 못하기 때문에 필터에서 발생한 인증 오류를 GlobalExceptionHandler로 전달하는 역할
@Component //빈등록
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final HandlerExceptionResolver resolver; //Spring이 예외를 처리하는 객체

    //HandlerExceptionResolver 타입으로 만들어진 객체가 2개 이상
    //그 중 원하는 객체의 주소값을 DI 받기 위해 @Qualifier으로 ID값을 작성하면 특정 객체의 주소값 1개가 DI된다.
    public JwtAuthenticationEntryPoint(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    // Security에서 인증 오류 나면 실행. request에 미리 담아둔 예외를 꺼내서(getAttribute("exception"))
    // resolver를 통해 GlobalExceptionHandler로 전달
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        resolver.resolveException(request, response, null, (Exception)request.getAttribute("exception"));
    }
}