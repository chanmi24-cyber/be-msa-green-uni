package com.green.gateway.filter;

import com.green.common.model.JwtMember;
import com.green.common.model.UserPrincipal;
import com.green.common.security.JwtTokenManager;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

// 모든 요청마다 실행되는 JWT 인증 필터
// AT 검증 → Security 인증 처리 → 다음 필터/컨트롤러에 유저 정보 헤더로 전달
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenManager jwtTokenManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("req-uri: {}", request.getRequestURI()); //요청 주소가 로그에 출력

        // 쿠키에서 AT 꺼내 검증 / 로그인 안 된 상태면 null 반환
        Authentication authentication = jwtTokenManager.getAuthentication(request);
        log.info("authentication: {}", authentication);

        HttpServletRequest requestToUse = request;

        try {
            if (authentication != null) {  //로그인 상태
                SecurityContextHolder.getContext().setAuthentication(authentication); // @AuthenticationPrincipal로 유저 정보 꺼낼 수 있음

                // 인증 정보가 있는 경우
                if (authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
                    JwtMember jwtMember = userPrincipal.getJwtMember();
                    log.info("================== jwtMember: {}", jwtMember);
                    String memberRole = jwtMember.getLoginMemberRole();

                    // 원본 요청을 감싸서 새 요청 생성
                    // 각 서비스가 토큰을 직접 파싱할 필요 없이 헤더에서 유저 정보를 꺼낼 수 있도록 설정
                    requestToUse = new HttpServletRequestWrapper(request) {
                        @Override
                        public String getHeader(String name) { // 헤더에서 정보 하나 꺼내기
                            if ("X-Member-Code".equals(name)) {
                                return String.valueOf(jwtMember.getLoginMemberCode());
                            }
                            if ("X-Member-Role".equals(name)) {
                                return memberRole;
                            }
                            if ("X-Device-Id".equals(name)) {
                                return jwtMember.getDeviceId();
                            }
                            return super.getHeader(name);
                        }

                        @Override
                        public Enumeration<String> getHeaderNames() { // 헤더에 무슨 값 저장되어있는지 목록 조회
                            List<String> names = Collections.list(super.getHeaderNames());
                            if (!names.contains("X-Member-Code")) names.add("X-Member-Code");
                            if (!names.contains("X-Member-Role")) names.add("X-Member-Role");
                            if (!names.contains("X-Device-Id")) names.add("X-Device-Id");
                            return Collections.enumeration(names);
                        }

                        @Override
                        public Enumeration<String> getHeaders(String name) { // 헤더에 저장된 값 하나 값이 여러개면 목록으로 조회
                            if ("X-Member-Code".equals(name)) {
                                return Collections.enumeration(Collections.singletonList(String.valueOf(jwtMember.getLoginMemberCode())));
                            }
                            if ("X-Member-Role".equals(name)) {
                                return Collections.enumeration(Collections.singletonList(memberRole));
                            }
                            if ("X-Device-Id".equals(name)) {
                                return Collections.enumeration(Collections.singletonList(jwtMember.getDeviceId()));
                            }
                            return super.getHeaders(name);
                        }
                    };
                }
            } else {
                // 로그인 안 된 상태 → 예외 처리 (Security가 이후에 처리)
                request.setAttribute("exception", new MalformedJwtException("토큰 확인"));
            }
        } catch (Exception e) {
            request.setAttribute("exception", e);
        }

        // 헤더가 추가된 request를 다음 필터로 전달
        filterChain.doFilter(requestToUse, response);
    }
}