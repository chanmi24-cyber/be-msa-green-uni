package com.green.core.application.course.model;

import com.green.common.enumcode.EnumScheduleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseStatusRes {
    private Boolean isOpen;
    private EnumScheduleType scheduleType;
}