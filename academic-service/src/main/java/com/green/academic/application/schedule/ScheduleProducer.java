package com.green.academic.application.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.academic.entity.Schedule;
import com.green.common.kafka.KafkaTopic;
import com.green.common.kafka.ScheduleEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleProducer {

    private final ScheduleRepository scheduleRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // @Scheduled(fixedDelay = 30_000) // 30초마다 테스트
    @Scheduled(cron = "0 0 9 * * *") // 매일 9시
    @Transactional
    public void updateActiveStatus() {
        LocalDateTime now = LocalDateTime.now();
        List<Schedule> schedules = scheduleRepository.findAll();

        for (Schedule schedule : schedules) {
            boolean shouldBeActive = !now.isBefore(schedule.getStartDate())
                    && !now.isAfter(schedule.getEndDate());
            schedule.updateActive(shouldBeActive);

            ScheduleEvent event = ScheduleEvent.builder()
                    .scheduleId(schedule.getScheduleId())
                    .type(schedule.getType())
                    .year(schedule.getYear())
                    .semester(schedule.getSemester())
                    .startDate(schedule.getStartDate())
                    .endDate(schedule.getEndDate())
                    .isActive(shouldBeActive)
                    .build();

            try {
                String eventJson = objectMapper.writeValueAsString(event);
                kafkaTemplate.send(KafkaTopic.SCHEDULE,
                        String.valueOf(schedule.getScheduleId()), eventJson);
            } catch (JsonProcessingException e) {
                log.error("Kafka 직렬화 실패: {}", e.getMessage());
            }
        }
        log.info("학사일정 활성화 상태 업데이트 완료");
    }
}