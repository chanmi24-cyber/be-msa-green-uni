package com.green.auth.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.auth.application.email.EmailSender;
import com.green.auth.kafka.model.TuitionMailEvent; // 🎯 auth 폴더 내부 이벤트 모델 유지
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TuitionMailConsumer {

    private final EmailSender emailSender;
    private final ObjectMapper objectMapper; // 🎯 Jackson ObjectMapper 주입

    @KafkaListener(topics = "tuition-mail-topic", groupId = "auth-email-group")
    // 🎯 중요: Payload를 String이나 객체가 아닌 byte[] 로 직접 받습니다!
    // 이렇게 하면 카프카가 패키지 검증(JsonDeserializer 규칙)을 하지 않고 날것 그대로 통과시켜 줍니다.
    public void consumeTuitionMail(@Payload byte[] messagePayload) {
        try {
            // 날것의 바이트 데이터를 auth 모듈이 가진 TuitionMailEvent 구조로 강제 매핑합니다.
            TuitionMailEvent event = objectMapper.readValue(messagePayload, TuitionMailEvent.class);

            log.info("등록금 미납 메일 발송 요청 수신: 학생코드 {}", event.getStudentCode());

            // setFrom이 없는 검증된 비밀번호 전송 메커니즘으로 발송 실행
            emailSender.sendRawHtmlMail(event.getEmail(), event.getTitle(), event.getContent());

        } catch (Exception e) {
            log.error("메일 수신 및 역직렬화 최종 실패: {}", e.getMessage(), e);
        }
    }
}