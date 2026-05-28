package com.green.core.application.grade.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GradeAppealProDetailRes {
    // 학생 정보
    private Long studentCode;
    private String studentName;
    private String majorName;
    private Integer academicYear;

    // 강의 정보
    private String lectureName;
    private Integer lectureYear;
    private Integer lectureSemester;

    // 이의신청 내용
    private String reason;
    private String appealStatus;
    private String rejectReason;
    private LocalDateTime createdAt;

    // 현재 성적 (수정 폼 초기값으로 사용)
    private Integer midScore;
    private Integer finScore;
    private Integer assignmentScore;
    private Integer attendScore;
    private Integer totalScore;
    private String gradeLetter;
}
