package com.green.academic.application.schedule;

import com.green.academic.application.schedule.model.*;
import com.green.academic.entity.Schedule;
import com.green.academic.exception.ScheduleErrorCode;
import com.green.common.auth.MemberContext;
import com.green.common.enumcode.EnumScheduleType;
import com.green.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    @Transactional
    public void createSchedule(ScheduleCreateReq req) {
        // 날짜 역전 체크 (시작일 > 종료일)
        if (req.getStartDate().isAfter(req.getEndDate())) {
            throw new BusinessException(ScheduleErrorCode.INVALID_DATE_RANGE);
        }

        Schedule schedule = Schedule.builder()
                .memberCode(MemberContext.get().memberCode())
                .title(req.getTitle())
                .year(req.getYear())
                .semester(req.getSemester())
                .type(req.getType())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .build();

        scheduleRepository.save(schedule);
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

    @Transactional(readOnly = true)
    public Map<EnumScheduleType, Boolean> getActiveSchedules() {
        List<Schedule> activeSchedules = scheduleRepository.findByIsActiveTrue();

        Map<EnumScheduleType, Boolean> data = new LinkedHashMap<>();
        for (EnumScheduleType type : EnumScheduleType.values()) {
            data.put(type, false);
        }
        for (Schedule schedule : activeSchedules) {
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
        scheduleRepository.delete(schedule);
    }

}