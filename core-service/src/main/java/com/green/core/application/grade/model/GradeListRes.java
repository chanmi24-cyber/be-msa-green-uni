package com.green.core.application.grade.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

// [추가] API-GPA-03: 교수 성적 조회 응답 (수강생 1명 단위)
@Getter
@AllArgsConstructor
public class GradeListRes {

    private Long courseId;
    private Long studentCode;
    private String memberName;
    @JsonProperty("academic_year")
    private Integer academicYear;
    private String majorName;
    @JsonProperty("mid_score")
    private Integer midScore;
    @JsonProperty("fin_score")
    private Integer finScore;
    @JsonProperty("assignment_score")
    private Integer assignmentScore;
    @JsonProperty("attend_score")
    private Integer attendScore;
    @JsonProperty("total_score")
    private Integer totalScore;
    @JsonProperty("grade_letter")
    private String gradeLetter;     // null = 미입력
}
