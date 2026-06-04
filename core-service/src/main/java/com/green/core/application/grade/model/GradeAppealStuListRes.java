package com.green.core.application.grade.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

// 학생 본인 이의신청 내역 조회 응답
@Getter
@AllArgsConstructor
public class GradeAppealStuListRes {

    private Long          courseId;
    private String        lectureName;
    private Integer       lectureYear;
    private Integer       lectureSemester;
    private String        reason;
    private String        appealStatus;    // PENDING / APPROVED / REJECTED
    private String        rejectReason;    // 반려 시에만 존재
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;    // 처리 완료 시에만 존재
}