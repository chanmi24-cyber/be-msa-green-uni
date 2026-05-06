package com.green.auth.kafka;

import com.green.auth.application.auth.AuthService;
import com.green.common.kafka.AuthMemberEvent;
import com.green.common.kafka.KafkaTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthMemberConsumer {
    private final AuthService authService;

//    @KafkaListener(topics = KafkaTopic.MEMBER, groupId = "auth-service-group")
//    public void consume(AuthMemberEvent event) {
//        log.info("AuthMemberEvent consumed: {}", event.getMemberCode());
//        authService.createAuthMember(AuthMemberCreateReq.builder()
//                .memberCode(event.getMemberCode())
//                .email(event.getEmail())
//                .password(event.getPassword())
//                .role(event.getRole())
//                .build());
//    }
}