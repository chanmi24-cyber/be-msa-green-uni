package com.green.academic.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Map;

// STOMP CONNECT 프레임 수신 시 핸드셰이크에서 저장한 사용자 정보를 Principal로 설정
// Principal.getName() = memberCode → /user/{memberCode}/queue/notifications 라우팅에 사용됨
@Slf4j
@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null) {
                Long memberCode = (Long) sessionAttributes.get("memberCode");
                String memberRole = (String) sessionAttributes.get("memberRole");

                if (memberCode != null) {
                    accessor.setUser(new MemberPrincipal(memberCode, memberRole));
                    log.info("WebSocket 연결 완료: memberCode={}, role={}", memberCode, memberRole);
                } else {
                    log.warn("WebSocket CONNECT: 인증 정보 없음 - 연결 허용하지만 개인 알림 수신 불가");
                }
            }
        }
        return message;
    }
}
