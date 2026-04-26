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
        String memberCodeStr = request.getHeader("X-User-Id"); // Gateway에서 보낸 PK 헤더
        String memberRole = request.getHeader("X-User-Role");

        if (memberCodeStr != null && memberRole != null) {
            try {
                // Integer로 파싱하여 객체 생성
                Integer memberCode = Integer.parseInt(memberCodeStr);
                MemberContext.set(new MemberDto(memberCode, memberRole));
            } catch (NumberFormatException e) {
                // 파싱 실패 시 로그만 남기고 통과 (인증 실패 처리는 Gateway나 Security가 담당)
            }
        }
        return true;
    }
        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
            MemberContext.clear();
        }
}