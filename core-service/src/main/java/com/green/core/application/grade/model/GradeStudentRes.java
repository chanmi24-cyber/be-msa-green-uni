package com.green.core.application.grade.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

// API-GPA-05: 학생 성적 조회 응답
@Getter
@AllArgsConstructor
public class GradeStudentRes {

    private List<GradeItem> gradeList;
    private Summary summary;

    @Getter
    @AllArgsConstructor
    public static class GradeItem {
        private Long courseId;
        private Integer lectureYear;
        private Integer lectureSemester;
        private String lectureName;
        private Integer lectureCredit;
        private String lectureType;
        private String lectureGrade;   // null = 성적 미입력 또는 강의평가 미완료
        private Double lectureRating;  // null = 성적 미입력 또는 강의평가 미완료
        private boolean evalCompleted; // 강의평가 완료 여부
    }

    @Getter
    @AllArgsConstructor
    public static class Summary {
        private double averageGpa;
        private int convertedScore;
        private int totalCredits;
    }
}