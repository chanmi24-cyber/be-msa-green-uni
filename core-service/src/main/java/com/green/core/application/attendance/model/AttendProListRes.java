package com.green.core.application.attendance.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AttendProListRes {
    private Long attendId;
    private Long studentCode;
    private String studentName;
    private Integer academicYear;
    private String status;
    private String reason;
}