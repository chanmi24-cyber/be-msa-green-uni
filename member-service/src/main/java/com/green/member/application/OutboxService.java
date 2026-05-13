package com.green.member.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.green.common.kafka.KafkaEvent;
import com.green.common.outbox.Outbox;
import com.green.common.outbox.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public void saveToOutbox(String topic, Long aggregateId, KafkaEvent event) {
        log.info("saveToOutbox 호출됨 - topic: {}, aggregateId: {}", topic, aggregateId);
        try {
            String payload = objectMapper.writeValueAsString(event);
            Outbox outbox = Outbox.builder()
                    .topic(topic)
                    .aggregateId(aggregateId)
                    .eventType(event.getEventType().name())
                    .payload(payload)
                    .build();
            outboxRepository.save(outbox);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Outbox 직렬화 실패", e);
        }
    }
}
