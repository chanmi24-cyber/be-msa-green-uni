package com.green.core.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.core.application.lecture.LectureAutoCancelService;
import com.green.common.enumcode.EnumScheduleType;
import com.green.common.kafka.KafkaTopic;
import com.green.core.entity.cache.ScheduleCache;
import com.green.core.repository.ScheduleCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.green.common.kafka.ScheduleEvent;

import java.time.LocalDate;


@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleConsumer {

    private final ScheduleCacheRepository scheduleCacheRepository;
    private final LectureAutoCancelService lectureAutoCancelService;
    private final ObjectMapper objectMapper;

    @Transactional
    @KafkaListener(topics = KafkaTopic.SCHEDULE, groupId = "core-service-group")
    public void consume(String message) throws JsonProcessingException {
        ScheduleEvent event = objectMapper.readValue(message, ScheduleEvent.class);
        log.info("Schedule 이벤트 수신: {}", event);

        scheduleCacheRepository.deleteByScheduleId(event.getScheduleId());

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

        // 수강신청 기간 종료 시 자동 폐강 처리
        LocalDate today = LocalDate.now();

        boolean courseRegEnded = event.getType() == EnumScheduleType.COURSE_REGISTRATION
                && !event.getIsActive()
                && today.equals(event.getEndDate().toLocalDate().plusDays(1));

        boolean modEnded = event.getType() == EnumScheduleType.COURSE_MODIFICATION
                && !event.getIsActive()
                && today.equals(event.getEndDate().toLocalDate().plusDays(1));

        if (courseRegEnded || modEnded) {
            boolean modExists = scheduleCacheRepository
                    .existsByTypeAndYearAndSemester(
                            EnumScheduleType.COURSE_MODIFICATION,
                            event.getYear(),
                            event.getSemester()
                    );

            if (courseRegEnded && modExists) {
                log.info("정정기간 예정됨 - 자동폐강 스킵");
                return;
            }

            log.info("수강신청/정정기간 종료 확인 - 자동폐강 실행");
            lectureAutoCancelService.autoCancelLectures(event.getYear(), event.getSemester());
        }
    }

    @KafkaListener(topics = KafkaTopic.SCHEDULE_DELETE, groupId = "core-service-group")
    @Transactional
    public void consumeDelete(String message) throws JsonProcessingException {
        ScheduleEvent event = objectMapper.readValue(message, ScheduleEvent.class);
        log.info("Schedule 삭제 이벤트 수신: {}", event);
        scheduleCacheRepository.deleteByScheduleId(event.getScheduleId());
    }

}