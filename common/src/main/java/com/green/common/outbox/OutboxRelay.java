package com.green.common.outbox;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "outbox.enabled", havingValue = "true")
public class OutboxRelay {
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @PostConstruct
    public void init() {
        log.info("OutboxRelay 빈 등록됨");
    }

    // 10초마다 실행 (간격은 조절 가능)
    @Scheduled(fixedDelay = 10_000, initialDelay = 20_000)
    @Transactional
    public void publishEvents() {
        List<Outbox> waitingEvents = outboxRepository.findAll();
        log.info("OutboxRelay 실행됨 - 대기 이벤트 수: {}", waitingEvents.size());

        for (Outbox outbox : waitingEvents) {
            try {
                kafkaTemplate.send(outbox.getTopic(),
                        String.valueOf(outbox.getAggregateId()),
                        outbox.getPayload()).get(); // 동기 대기
                outboxRepository.deleteById(outbox.getId()); // 트랜잭션 내에서 삭제
                log.info("Outbox topic: {}", outbox.getTopic());
            } catch (Exception e) {
                log.error("카프카 전송 실패: {}", e.getMessage());
            }
        }
    }

}