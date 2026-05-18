package com.green.academic.kafka;

import com.green.academic.application.notification.NotificationRepository;
import com.green.academic.application.notification.model.NotiPushRes;
import com.green.academic.entity.Notification;
import com.green.common.constants.EventType;
import com.green.common.kafka.KafkaTopic;
import com.green.common.kafka.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    @KafkaListener(topics = KafkaTopic.NOTIFICATION, groupId = "academic-group")
    public void consume(NotificationEvent event) {
        if (event.getEventType() != EventType.E_CREATED) return;

        String targetRole = event.getTargetRole() != null ? event.getTargetRole().getCode() : null;

        Notification notification = Notification.builder()
                .memberCode(event.getMemberCode())
                .targetRole(targetRole)
                .type(event.getType())
                .message(event.getMessage())
                .url(event.getUrl())
                .refId(event.getRefId())
                .build();

        notificationRepository.save(notification);
        log.info("알림 저장: type={}, memberCode={}, targetRole={}", event.getType(), event.getMemberCode(), targetRole);

        pushToWebSocket(notification);
    }

    private void pushToWebSocket(Notification notification) {
        NotiPushRes res = NotiPushRes.from(notification);

        if (notification.getMemberCode() != null) {
            // 개인 알림: /user/{memberCode}/queue/notifications
            messagingTemplate.convertAndSendToUser(
                    notification.getMemberCode().toString(),
                    "/queue/notifications",
                    res
            );
            log.debug("WebSocket 개인 알림 push: memberCode={}", notification.getMemberCode());
        } else if (notification.getTargetRole() != null) {
            // 역할 브로드캐스트: /topic/STUDENT 또는 /topic/PROFESSOR
            messagingTemplate.convertAndSend(
                    "/topic/" + notification.getTargetRole(),
                    res
            );
            log.debug("WebSocket 역할 알림 push: role={}", notification.getTargetRole());
        }
    }
}
