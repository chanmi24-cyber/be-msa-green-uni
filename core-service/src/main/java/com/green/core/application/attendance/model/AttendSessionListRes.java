package com.green.core.application.attendance.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class AttendSessionListRes {
    private Long sessionId;
    private LocalDate classDate;
    private String sessionType;
    private Boolean isActive;
}