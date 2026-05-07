package com.green.core.kafka;

import com.green.common.kafka.KafkaTopic;
import com.green.core.entity.cache.ScheduleCache;
import com.green.core.repository.ScheduleCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.green.common.kafka.ScheduleEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleConsumer {

    private final ScheduleCacheRepository scheduleCacheRepository;

    @Transactional
    @KafkaListener(topics = KafkaTopic.SCHEDULE, groupId = "core-service-group")
    public void consume(ScheduleEvent event) {
        log.info("Schedule 이벤트 수신: {}", event);

        ScheduleCache cache = ScheduleCache.builder()
                .scheduleId(event.getScheduleId())
                .type(event.getType())
                .year(event.getYear())
                .semester(event.getSemester())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .isActive(event.getIsActive())
                .build();

        scheduleCacheRepository.save(cache);
    }
}