package com.green.member.kafka;

import com.green.common.enumcode.EnumScheduleType;
import com.green.common.kafka.KafkaTopic;
import com.green.common.kafka.ScheduleEvent;

import com.green.member.application.schedule.ScheduleCacheRepository;
import com.green.member.entity.cache.ScheduleCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleConsumer {

    private final ScheduleCacheRepository scheduleCacheRepository;

    @Transactional
    @KafkaListener(topics = KafkaTopic.SCHEDULE, groupId = "member-service-group")
    public void consume(ScheduleEvent event) {
        // MAJOR_CHANGE, SEMESTER_START, SEMESTER_END만 저장
        if (event.getType() != EnumScheduleType.MAJOR_CHANGE
                && event.getType() != EnumScheduleType.SEMESTER_START
                && event.getType() != EnumScheduleType.SEMESTER_END) return;
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

    @KafkaListener(topics = KafkaTopic.SCHEDULE_DELETE, groupId = "member-service-group")
    @Transactional
    public void consumeDelete(ScheduleEvent event) {
        log.info("Schedule 삭제 이벤트 수신: {}", event);
        scheduleCacheRepository.deleteByScheduleId(event.getScheduleId());
    }
}