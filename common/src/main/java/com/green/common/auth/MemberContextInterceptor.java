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
        String memberCodeTK = request.getHeader("X-Member-Code");
        String memberRoleTK = request.getHeader("X-Member-Role");

        if (memberCodeTK != null && memberRoleTK != null) {
            try {
                Integer memberCode = Integer.parseInt(memberCodeTK);
                EnumMemberRole memberRole = EnumMemberRole.valueOf(memberRoleTK);
                MemberContext.set(new MemberDto(memberCode, memberRole));
            } catch (NumberFormatException e) { // 파싱 실패 시 통과
            }
        }
        return true;
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MemberContext.clear();
    }
}