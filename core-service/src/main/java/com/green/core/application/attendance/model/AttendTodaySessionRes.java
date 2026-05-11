package com.green.core.application.attendance.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class AttendTodaySessionRes {
    private Long sessionId;
    private LocalDate classDate;
    private Boolean isActive; // true=출석진행중, false=출석완료
}