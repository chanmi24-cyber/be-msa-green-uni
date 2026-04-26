package com.green.common.auth;

import com.green.common.model.EnumMemberRole;
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
        String memberCode = request.getHeader("X-Member-Id");
        String role = request.getHeader("X-Member-Role");

        if (memberCode != null && role != null) {
            MemberContext.set(new MemberDto(
                    Integer.parseInt(memberCode),
                    EnumMemberRole.valueOf(role)
            ));
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MemberContext.clear(); // 메모리 누수 방지를 위해 반드시 삭제
    }
}