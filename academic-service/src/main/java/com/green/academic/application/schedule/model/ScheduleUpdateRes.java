package com.green.academic.application.schedule.model;

import com.green.common.enumcode.EnumScheduleType;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ScheduleUpdateRes {
    private Long scheduleId;
    private Integer semester;
    private String title;
    private EnumScheduleType type;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private LocalDateTime updatedAt;
}