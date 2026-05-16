package com.green.academic.kafka;

import com.green.common.constants.EventType;
import com.green.common.enumcode.EnumMemberRole;
import com.green.common.kafka.KafkaTopic;
import com.green.common.kafka.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AcademicNotificationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendToRole(String type, String message, EnumMemberRole targetRole, Long refId, String url) {
        NotificationEvent event = NotificationEvent.builder()
                .eventType(EventType.E_CREATED)
                .targetRole(targetRole)
                .type(type)
                .message(message)
                .url(url)
                .refId(refId)
                .build();
        kafkaTemplate.send(KafkaTopic.NOTIFICATION, event);
        log.info("알림 발행: type={}, targetRole={}", type, targetRole);
    }

    public void sendToMember(String type, String message, Long memberCode, Long refId, String url) {
        NotificationEvent event = NotificationEvent.builder()
                .eventType(EventType.E_CREATED)
                .memberCode(memberCode)
                .type(type)
                .message(message)
                .url(url)
                .refId(refId)
                .build();
        kafkaTemplate.send(KafkaTopic.NOTIFICATION, event);
        log.info("알림 발행: type={}, memberCode={}", type, memberCode);
    }
}
