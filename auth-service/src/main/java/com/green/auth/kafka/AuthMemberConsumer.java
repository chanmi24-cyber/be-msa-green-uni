package com.green.auth.kafka;

import com.green.auth.application.auth.AuthService;
import com.green.common.constants.EventType;
import com.green.common.enumcode.EnumMemberRole;
import com.green.common.kafka.auth.AuthMemberEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.green.common.kafka.member.MemberTopic;
import com.green.auth.application.auth.model.AuthMemberCreateReq;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthMemberConsumer {
    private final AuthService authService;

    @KafkaListener(topics = MemberTopic.AUTH_MEMBER, groupId = "auth-service-group")
    public void consume(AuthMemberEvent event) {
        log.info("AuthMemberEvent consumed: {}", event.getMemberCode());
        EventType type = event.getEventType();
        if (type == EventType.E_CREATED) {
            authService.createAuthMember(AuthMemberCreateReq.builder()
                    .memberCode(event.getMemberCode())
                    .email(event.getEmail())
                    .password(event.getPassword())
                    .role(EnumMemberRole.from(event.getRole()))
                    .build());
        } else if (type == EventType.E_UPDATED) {
            if ("EMAIL".equals(event.getUpdateType())) {
                authService.updateEmail(event.getMemberCode(), event.getEmail());
            } else if ("DEACTIVATE".equals(event.getUpdateType())) {
                authService.deactivate(event.getMemberCode());
            }
        }
    }
}