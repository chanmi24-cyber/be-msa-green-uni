package com.green.core.kafka;

import com.green.common.kafka.KafkaTopic;
import com.green.common.kafka.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendNotification(NotificationEvent event) {
        kafkaTemplate.send(KafkaTopic.NOTIFICATION, event);
        log.info("NotificationEvent sent: {}", event.getType());
    }
}