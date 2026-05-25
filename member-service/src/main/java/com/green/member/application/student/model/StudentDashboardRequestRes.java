package com.green.member.application.student.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StudentDashboardRequestRes {
    private Long requestId;
    private String requestCategory; // STATUS | MAJOR
    private String type;
    private String targetMajorName; // 전공변경 신청에만 값 존재, 학적변경은 null
    private String status;
    private Integer academicYear;
    private Integer semester;
    private LocalDateTime createdAt;
}