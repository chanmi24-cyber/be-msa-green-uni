package com.green.core.kafka;

import com.green.common.constants.EventType;
import com.green.common.kafka.KafkaTopic;
import com.green.common.kafka.StudentMajorEvent;
import com.green.core.entity.cache.StudentMajorCache;
import com.green.core.repository.StudentMajorCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentMajorConsumer {
    private final StudentMajorCacheRepository studentMajorCacheRepository;

    @Transactional
    @KafkaListener(topics = KafkaTopic.STUDENT_MAJOR, groupId = "core-service-group")
    public void consume(StudentMajorEvent event) {
        log.info("Kafka 메시지 수신: {}", event);

        try {
            EventType type = event.getEventType();
            if (type == EventType.E_CREATED || type == EventType.E_UPDATED) {
                StudentMajorCache cache = StudentMajorCache.builder()
                        .studentMajorId(event.getStudentMajorId())
                        .studentCode(event.getStudentCode())
                        .majorId(event.getMajorId())
                        .type(event.getType())
                        .isActive(event.getIsActive())
                        .build();
                studentMajorCacheRepository.save(cache);
                log.info("studentMajorCache 저장 완료: {}", event.getStudentMajorId());
            } else if (type == EventType.E_DELETED) {
                studentMajorCacheRepository.deleteById(event.getStudentMajorId());
                log.info("삭제 완료: {}", event.getStudentMajorId());
            }
        } catch (Exception e) {
            log.error("Kafka 메시지 처리 중 오류 발생: ", e);
        }
    }
}
