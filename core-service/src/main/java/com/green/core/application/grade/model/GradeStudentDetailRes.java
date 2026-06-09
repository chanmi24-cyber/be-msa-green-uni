package com.green.core.application.grade.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

// API-GPA-06: 학생 전체 성적 상세조회 응답 (학생 정보 + 전 강의 점수 내역 + 요약)
@Getter
@AllArgsConstructor
public class GradeStudentDetailRes {

    private StudentInfo     studentInfo;
    private List<GradeItem> gradeList;
    private Summary         summary;
    private boolean         appealPeriod;

    @Getter
    @AllArgsConstructor
    public static class StudentInfo {
        private String  name;
        private String  majorName;
        private String  collegeName;
        private Integer academicYear;
        private Integer semester;
    }

    @Getter
    @AllArgsConstructor
    public static class GradeItem {
        private Long    courseId;
        private Integer lectureYear;
        private Integer lectureSemester;
        private String  lectureName;
        private Integer lectureCredit;
        private String  lectureType;
        private int     midScore;
        private int     finScore;
        private int     assignmentScore;
        private int     attendScore;
        private int     totalScore;
        private String  lectureGrade;    // null = 성적 미입력
        private Double  lectureRating;   // null = 성적 미입력
        private Integer myRank;          // null = 성적 미입력
        private Integer totalCount;      // null = 성적 미입력
        private String  appealStatus;    // null (추후 구현)
        private boolean canAppeal;       // 현재 학기 + 기간 활성일 때만 true
    }

    @Getter
    @AllArgsConstructor
    public static class Summary {
        private double averageGpa;       // 평점 평균
        private int    convertedScore;   // 환산 점수
        private int    totalCredits;     // 취득 학점
        private double averageScore;     // 실점 평균 (총점 기준)
        private String averageGrade;     // 등급 평균
        private int    majorRank;        // 학과 석차
        private int    majorTotalCount;  // 학과 재학생 수
    }
}