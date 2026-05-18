package com.green.auth.kafka;

import com.green.auth.application.auth.AuthService;
import com.green.common.constants.EventType;
import com.green.common.constants.UpdateType;
import com.green.common.enumcode.EnumMemberRole;
import com.green.common.exception.BusinessException;
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
        try {
            EventType type = event.getEventType();
            if (type == EventType.E_CREATED) {
                authService.createAuthMember(AuthMemberCreateReq.builder()
                        .memberCode(event.getMemberCode())
                        .email(event.getEmail())
                        .password(event.getPassword())
                        .role(EnumMemberRole.from(event.getRole()))
                        .build());
            } else if (type == EventType.E_UPDATED) {
                if (UpdateType.EMAIL.equals(event.getUpdateType())) {
                    authService.updateEmail(event.getMemberCode(), event.getEmail());
                } else if (UpdateType.DEACTIVATE.equals(event.getUpdateType())) {
                    authService.deactivate(event.getMemberCode());
                }
            }
        } catch (BusinessException e) {
            log.warn("AuthMemberEvent 처리 중 비즈니스 오류. memberCode: {}, error: {}", event.getMemberCode(), e.getMessage());
        } catch (Exception e) {
            log.error("AuthMemberEvent 처리 실패. memberCode: {}, error: {}", event.getMemberCode(), e.getMessage());
        }
    }
}