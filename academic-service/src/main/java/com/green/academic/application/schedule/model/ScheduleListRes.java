package com.green.academic.application.schedule.model;

import com.green.common.enumcode.EnumScheduleType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ScheduleListRes {
    private Long scheduleId;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private EnumScheduleType type;
    private Boolean isActive;
}