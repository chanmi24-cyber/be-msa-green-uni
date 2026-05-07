package com.green.academic.application.schedule;

import com.green.academic.application.schedule.model.ScheduleActiveRes;
import com.green.academic.application.schedule.model.ScheduleCreateReq;
import com.green.academic.application.schedule.model.ScheduleListReq;
import com.green.academic.application.schedule.model.ScheduleListRes;
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
}