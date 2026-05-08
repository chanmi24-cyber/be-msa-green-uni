package com.green.academic.application.schedule.model;

import com.green.common.enumcode.EnumScheduleType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ScheduleCreateReq {
    private String title;
    private Integer year;
    private Integer semester;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private EnumScheduleType type;
}