package com.green.academic.application.schedule.model;

import com.green.common.enumcode.EnumScheduleType;
import lombok.Builder;
import lombok.Getter;
import java.util.Map;

@Getter
@Builder
public class ScheduleActiveRes {
    private Map<EnumScheduleType, Boolean> data;
}