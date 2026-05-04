package com.green.core.kafka;

import com.green.common.constants.EventType;
import com.green.common.kafka.KafkaTopic;
import com.green.common.kafka.StudentEvent;
import com.green.core.entity.cache.StudentCache;
import com.green.core.repository.StudentCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentConsumer {
    private final StudentCacheRepository studentCacheRepository;

    @Transactional
    @KafkaListener(topics = KafkaTopic.STUDENT, groupId = "core-service-group")
    public void consume(StudentEvent event) {
        log.info("Kafka 메시지 수신: {}", event);

        try {
            EventType type = event.getEventType();
            if (type == EventType.E_CREATED || type == EventType.E_UPDATED ) {
                // 저장 또는 수정 (Idempotent: 동일 ID면 덮어쓰기 됨)
                StudentCache cache = StudentCache.builder()
                        .memberCode(event.getMemberCode())
                        .name(event.getName())
                        .build();
                studentCacheRepository.save(cache);
                log.info("studentCache 정보저장 완료: {}", event.getMemberCode());
            }else if (type == EventType.E_DELETED) {
                // 삭제
                studentCacheRepository.deleteById(event.getMemberCode());
                log.info("삭제 완료: {}", event.getMemberCode());
            }

        } catch (Exception e) {
            log.error("Kafka 메시지 처리 중 오류 발생: ", e);
        }
    }
}
