package com.green.core.application.attendance.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AttendSessionStartRes {
    private Long sessionId;
    private Boolean isActive;
    private LocalDateTime startedAt;
    private String lectureName;
    private String lectureRoom;
}