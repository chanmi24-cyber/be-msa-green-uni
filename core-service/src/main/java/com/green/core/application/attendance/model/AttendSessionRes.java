package com.green.core.application.attendance.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

// [추가] AttendSessionStartRes에서 이름 변경 + originalDate 추가
//        일반 세션: originalDate = null, 보강 세션: originalDate = 휴강 원래 날짜
@Getter
@AllArgsConstructor
public class AttendSessionRes {
    private Long sessionId;
    private Boolean isActive;
    private LocalDateTime startedAt;
    private String lectureName;
    private String lectureRoom;
    private LocalDate originalDate;
}