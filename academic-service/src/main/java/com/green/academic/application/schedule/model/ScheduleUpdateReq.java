package com.green.academic.application.schedule.model;

import com.green.common.enumcode.EnumScheduleType;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ScheduleUpdateReq {
    private String title;
    private Integer semester;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private EnumScheduleType type;
}