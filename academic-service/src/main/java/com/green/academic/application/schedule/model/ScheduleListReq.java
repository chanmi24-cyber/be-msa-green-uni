package com.green.academic.application.schedule.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleListReq {
    private Integer year;
    private Integer semester;
    private Integer targetMonth;
    private String viewType;
}