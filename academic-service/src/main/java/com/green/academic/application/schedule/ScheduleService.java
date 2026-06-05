package com.green.academic.application.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.academic.application.schedule.model.*;
import com.green.academic.entity.Schedule;
import com.green.academic.exception.ScheduleErrorCode;
import com.green.common.auth.MemberContext;
import com.green.common.enumcode.EnumScheduleType;
import com.green.common.exception.BusinessException;
import com.green.common.kafka.KafkaTopic;
import com.green.common.kafka.ScheduleEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public void createSchedule(ScheduleCreateReq req) {
        // 날짜 역전 체크 (시작일 > 종료일)
        if (req.getStartDate().isAfter(req.getEndDate())) {
            throw new BusinessException(ScheduleErrorCode.INVALID_DATE_RANGE);
        }

        LocalDateTime now = LocalDateTime.now();
        boolean isActive = !now.isBefore(req.getStartDate()) && !now.isAfter(req.getEndDate());

        Schedule schedule = Schedule.builder()
                .memberCode(MemberContext.get().memberCode())
                .title(req.getTitle())
                .year(req.getYear())
                .semester(req.getSemester())
                .type(req.getType())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .isActive(isActive)
                .build();

        Schedule saved = scheduleRepository.saveAndFlush(schedule);
        log.info("저장 완료 - scheduleId: {}", saved.getScheduleId());
        sendKafkaEvent(schedule);
    }

    @Transactional(readOnly = true)
    public List<ScheduleListRes> getSchedules(ScheduleListReq req) {
        return scheduleRepository.findAll(ScheduleSpec.filter(req))
                .stream()
                .map(s -> new ScheduleListRes(
                        s.getScheduleId(),
                        s.getTitle(),
                        s.getStartDate().toLocalDate(),
                        s.getEndDate().toLocalDate(),
                        s.getType(),
                        s.getIsActive()
                ))
                .toList();
    }

    //학사일정에 따른 메뉴 활성화 로직
    @Transactional(readOnly = true)
    public Map<EnumScheduleType, Boolean> getActiveSchedules() {
        List<Schedule> activeSchedules = scheduleRepository.findByIsActiveTrue();

        Map<EnumScheduleType, Boolean> data = new LinkedHashMap<>();
        for (EnumScheduleType type : EnumScheduleType.values()) {
            if (type == EnumScheduleType.ETC) continue;
            data.put(type, false);
        }
        for (Schedule schedule : activeSchedules) {
            if (schedule.getType() == EnumScheduleType.ETC) continue;
            data.put(schedule.getType(), true);
        }
        return data;
    }

    @Transactional
    public ScheduleUpdateRes updateSchedule(Long scheduleId, ScheduleUpdateReq req) {
        if (req.getStartDate().isAfter(req.getEndDate())) {
            throw new BusinessException(ScheduleErrorCode.INVALID_DATE_RANGE);
        }
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));
        schedule.update(req.getTitle(), req.getSemester(), req.getStartDate(),
                req.getEndDate(), req.getType());

        LocalDateTime now = LocalDateTime.now();
        boolean isActive = !now.isBefore(schedule.getStartDate()) && !now.isAfter(schedule.getEndDate());
        schedule.updateActive(isActive);

        sendKafkaEvent(schedule);

        return ScheduleUpdateRes.builder()
                .scheduleId(schedule.getScheduleId())
                .semester(schedule.getSemester())
                .title(schedule.getTitle())
                .type(schedule.getType())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .isActive(schedule.getIsActive())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }

    @Transactional
    public void deleteSchedule(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        sendKafkaDeleteEvent(schedule);
        scheduleRepository.delete(schedule);
    }

    // ===== Kafka 발행 공통 메서드 =====
    private void sendKafkaEvent(Schedule schedule) {
        log.info("Kafka 발행 시작 - scheduleId: {}", schedule.getScheduleId());
        try {
            ScheduleEvent event = ScheduleEvent.builder()
                    .scheduleId(schedule.getScheduleId())
                    .type(schedule.getType())
                    .year(schedule.getYear())
                    .semester(schedule.getSemester())
                    .startDate(schedule.getStartDate())
                    .endDate(schedule.getEndDate())
                    .isActive(schedule.getIsActive())
                    .build();
            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaTopic.SCHEDULE,
                    String.valueOf(schedule.getScheduleId()), eventJson);
            log.info("Kafka 발행 완료"); // 추가!
        } catch (JsonProcessingException e) {
            log.error("Kafka 직렬화 실패: {}", e.getMessage());
        }
    }

    private void sendKafkaDeleteEvent(Schedule schedule) {
        log.info("Kafka 발행 시작 - scheduleId: {}", schedule.getScheduleId());
        try {
            ScheduleEvent event = ScheduleEvent.builder()
                    .scheduleId(schedule.getScheduleId())
                    .type(schedule.getType())
                    .year(schedule.getYear())
                    .semester(schedule.getSemester())
                    .startDate(schedule.getStartDate())
                    .endDate(schedule.getEndDate())
                    .isActive(false)  // 삭제니까 false
                    .build();
            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaTopic.SCHEDULE_DELETE,
                    String.valueOf(schedule.getScheduleId()), eventJson);
            log.info("Kafka 발행 완료"); // 추가
        } catch (JsonProcessingException e) {
            log.error("Kafka 직렬화 실패: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ScheduleBannerRes getActiveBannerSchedule() {
        return scheduleRepository.findByIsActiveTrue().stream()
                .filter(s -> s.getType() != EnumScheduleType.ETC)
                .findFirst()
                .map(s -> ScheduleBannerRes.builder()
                        .type(s.getType().getCode())
                        .title(s.getTitle())
                        .startDate(s.getStartDate().toLocalDate())
                        .endDate(s.getEndDate().toLocalDate())
                        .build())
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<ScheduleBannerRes> getActiveBannerSchedules() {
        return scheduleRepository.findByIsActiveTrue().stream()
                .filter(s -> s.getType() != EnumScheduleType.ETC)
                .map(s -> ScheduleBannerRes.builder()
                        .type(s.getType().getCode())
                        .title(s.getTitle())
                        .startDate(s.getStartDate().toLocalDate())
                        .endDate(s.getEndDate().toLocalDate())
                        .build())
                .toList();
    }
}