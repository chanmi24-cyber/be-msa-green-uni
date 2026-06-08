package com.green.academic.application.schedule;

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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SimpMessagingTemplate messagingTemplate;

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
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                messagingTemplate.convertAndSend("/topic/banner", "refresh");
            }
        });
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
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                messagingTemplate.convertAndSend("/topic/banner", "refresh");
            }
        });

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
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                messagingTemplate.convertAndSend("/topic/banner", "refresh");
            }
        });
    }

    // ===== Kafka 발행 공통 메서드 =====
    private void sendKafkaEvent(Schedule schedule) {
        log.info("Kafka 발행 시작 - scheduleId: {}", schedule.getScheduleId());
        ScheduleEvent event = ScheduleEvent.builder()
                .scheduleId(schedule.getScheduleId())
                .type(schedule.getType())
                .year(schedule.getYear())
                .semester(schedule.getSemester())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .isActive(schedule.getIsActive())
                .build();
        kafkaTemplate.send(KafkaTopic.SCHEDULE, String.valueOf(schedule.getScheduleId()), event);
        log.info("Kafka 발행 완료");
    }

    private void sendKafkaDeleteEvent(Schedule schedule) {
        log.info("Kafka 발행 시작 - scheduleId: {}", schedule.getScheduleId());
        ScheduleEvent event = ScheduleEvent.builder()
                .scheduleId(schedule.getScheduleId())
                .type(schedule.getType())
                .year(schedule.getYear())
                .semester(schedule.getSemester())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .isActive(false)
                .build();
        kafkaTemplate.send(KafkaTopic.SCHEDULE_DELETE, String.valueOf(schedule.getScheduleId()), event);
        log.info("Kafka 발행 완료");
    }

    @Transactional(readOnly = true)
    public ScheduleBannerRes getActiveBannerSchedule() {
        LocalDateTime now = LocalDateTime.now();
        return scheduleRepository.findAll().stream()
                .filter(s -> s.getType() != EnumScheduleType.ETC)
                .filter(s -> !now.isBefore(s.getStartDate()) && !now.isAfter(s.getEndDate()))
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
        LocalDateTime now = LocalDateTime.now();
        return scheduleRepository.findAll().stream()
                .filter(s -> s.getType() != EnumScheduleType.ETC)
                .filter(s -> !now.isBefore(s.getStartDate()) && !now.isAfter(s.getEndDate()))
                .map(s -> ScheduleBannerRes.builder()
                        .type(s.getType().getCode())
                        .title(s.getTitle())
                        .startDate(s.getStartDate().toLocalDate())
                        .endDate(s.getEndDate().toLocalDate())
                        .build())
                .toList();
    }
}