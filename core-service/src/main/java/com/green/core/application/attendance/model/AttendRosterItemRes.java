package com.green.core.application.attendance.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AttendRosterItemRes {
    private Long attendId;
    private Long studentCode;
    private String studentName;
    private Integer academicYear;
    private String status;   // 한글값: 출석/결석/지각/조퇴
    private String reason;
}
