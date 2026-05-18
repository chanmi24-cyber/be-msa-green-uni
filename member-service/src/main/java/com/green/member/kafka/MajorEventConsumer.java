package com.green.member.kafka;

import com.green.common.constants.EventType;
import com.green.common.kafka.KafkaTopic;
import com.green.common.kafka.MajorEvent;
import com.green.member.entity.cache.MajorCache;
import com.green.member.application.major.MajorCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MajorEventConsumer {
    private final MajorCacheRepository majorCacheRepository;

    @Transactional
    @KafkaListener(topics = KafkaTopic.MAJOR, groupId = "member-service-group")
    public void consume(MajorEvent event) {
        log.info("Kafka 메시지 수신: {}", event);

        try {
            EventType type = event.getEventType();
            if (type == EventType.E_CREATED || type == EventType.E_UPDATED ) {
                // 저장 또는 수정 (Idempotent: 동일 ID면 덮어쓰기 됨)
                MajorCache cache = MajorCache.builder()
                        .majorId(event.getMajorId())
                        .name(event.getName())
                        .collegeId(event.getCollegeId())
                        .collegeName(event.getCollegeName())
                        .active(event.getActive())
                        .build();
                majorCacheRepository.save(cache);
                log.info("MajorCache 정보저장 완료: {}", event.getMajorId());
            } else if (type == EventType.E_DELETED) {
                // 삭제
                majorCacheRepository.deleteById(event.getMajorId());
                log.info("삭제 완료: {}", event.getMajorId());
            }

        } catch (Exception e) {
            log.error("Kafka 메시지 처리 중 오류 발생: ", e);
        }
    }
}
