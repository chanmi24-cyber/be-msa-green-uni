package com.green.academic.application.schedule.model;

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
    private String type;  // EnumScheduleType → String으로 변경
    private Boolean isActive;
}