package com.green.core.application.grade.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

// [추가] API-GPA-02: 교수 성적 입력/수정 요청 (수강생 1명 단위)
@Getter
@NoArgsConstructor
public class GradeUpdateReq {

    private Long courseId;          // 수강 ID (Grade PK)
    private Integer midScore;       // 중간 점수 (0~100)
    private Integer finScore;       // 기말 점수 (0~100)
    private Integer assignmentScore; // 과제 점수 (0~100)
}