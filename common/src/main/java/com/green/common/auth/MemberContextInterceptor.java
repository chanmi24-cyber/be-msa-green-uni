package com.green.common.auth;

import com.green.common.model.MemberDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

//각 서비스에 로그인 정보를 전달하는 역할
@Component
public class MemberContextInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = request.getHeader("X-User-Id");
        String userName = request.getHeader("X-User-Name");

        if (userId != null && userName != null) {
            try {
                String decodedUserName = URLDecoder.decode(userName, StandardCharsets.UTF_8);
                MemberContext.set(new MemberDto(Long.parseLong(userId), decodedUserName));
            } catch (Exception e) {
                // 디코딩 실패 시 원본 사용 (또는 무시)
                MemberContext.set(new MemberDto(Long.parseLong(userId), userName));
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MemberContext.clear(); // 메모리 누수 방지를 위해 반드시 삭제
    }
}