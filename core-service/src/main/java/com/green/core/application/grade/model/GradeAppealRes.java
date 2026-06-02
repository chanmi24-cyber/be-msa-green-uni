package com.green.core.application.grade.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

// API-GPA-07: 이의신청 폼 사전 조회 응답 (강의 정보 + 학생 정보 + 기존 신청 상태)
@Getter
@AllArgsConstructor
public class GradeAppealRes {

    // 강의 정보
    private String  lectureName;
    private Integer lectureYear;
    private Integer lectureSemester;
    private String  professorName;

    // 학생 정보
    private Long    memberCode;     // 학번
    private String  studentName;
    private String  majorName;
    private Integer academicYear;

    // 기존 이의신청 정보 (신청 이력 없으면 null)
    private String  appealStatus;   // PENDING / APPROVED / REJECTED
    private String  existingReason;
}