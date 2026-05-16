package com.green.academic.websocket;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

// HTTP 업그레이드(핸드셰이크) 시점에 게이트웨이가 주입한 X-Member-* 헤더를 WebSocket 세션에 저장
@Slf4j
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String memberCode = httpRequest.getHeader("X-Member-Code");
            String memberRole = httpRequest.getHeader("X-Member-Role");

            if (memberCode != null && memberRole != null) {
                attributes.put("memberCode", Long.parseLong(memberCode));
                attributes.put("memberRole", memberRole);
                log.debug("WebSocket 핸드셰이크: memberCode={}, role={}", memberCode, memberRole);
            } else {
                log.warn("WebSocket 핸드셰이크: 인증 헤더 없음");
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {}
}
