package com.green.auth.kafka;

import com.green.auth.application.email.EmailSender;
import com.green.auth.kafka.model.TuitionMailEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TuitionMailConsumer {

    private final EmailSender emailSender;

    @KafkaListener(topics = "tuition-mail-topic", groupId = "auth-email-group")
    public void consumeTuitionMail(TuitionMailEvent event) {
        log.info("등록금 미납 메일 발송 요청 수신: 학생코드 {}", event.getStudentCode());
        try {
            emailSender.sendRawHtmlMail(event.getEmail(), event.getTitle(), event.getContent());
        } catch (Exception e) {
            log.error("메일 실제 발송 실패: {}", e.getMessage());
        }
    }
}