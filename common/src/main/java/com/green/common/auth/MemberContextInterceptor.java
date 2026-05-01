package com.green.common.auth;

import com.green.common.enumcode.EnumMemberRole;
import com.green.common.model.MemberDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

// 게이트웨이가 심어준 헤더에서 로그인 정보를 꺼내 각 서비스에 전달하는 역할
@Component
public class MemberContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // TokenAuthenticationFilter가 심어준 헤더에서 유저 정보 꺼내기
        String memberCodeTK = request.getHeader("X-Member-Code");
        String memberRoleTK = request.getHeader("X-Member-Role");
        String deviceIdTK = request.getHeader("X-Device-Id");

        if (memberCodeTK != null && memberRoleTK != null) {
            try {
                // 문자열로 온 값들을 각각 맞는 타입으로 변환
                Long memberCode = Long.parseLong(memberCodeTK);
                EnumMemberRole memberRole = EnumMemberRole.valueOf(memberRoleTK);
                // 변환된 값을 저장
                MemberContext.set(new MemberDto(memberCode, memberRole, deviceIdTK));
            } catch (NumberFormatException e) { // 파싱 실패 시 저장 안 하고 통과 (Security가 이후에 처리)
            }
        }
        return true; // true 반환해야 다음 단계로 진행됨
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 요청 처리 완료 후 반드시 초기화
        // 안 지우면 다음 요청에서 이전 유저 정보가 남아있는 오류 발생
        MemberContext.clear();
    }
}