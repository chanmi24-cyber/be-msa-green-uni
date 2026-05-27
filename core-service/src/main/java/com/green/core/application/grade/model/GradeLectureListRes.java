package com.green.core.application.grade.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

// [추가] 교수 강의 목록 응답 DTO — GET /professor/grades/lectures
@Getter
@AllArgsConstructor
public class GradeLectureListRes {
    private Long lectureId;
    private String lectureName;
    private String lectureType;   // EnumLectureType.getValue() 한글값
    private Integer year;
    private Integer semester;
    private Integer credit;
    @JsonProperty("academic_year")
    private Integer academicYear;
}