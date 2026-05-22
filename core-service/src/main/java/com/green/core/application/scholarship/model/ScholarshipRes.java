package com.green.core.application.scholarship.model;

import com.green.core.entity.scholarship.Scholarship;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ScholarshipRes {
    private Long memberCode;
    private String studentName;
    private String deptName;
    private Integer academicYear;
    private Integer year;
    private Integer semester;
    private String scholarshipType;
    private Long scholarshipAmount;
    private LocalDateTime createdAt;

    public static ScholarshipRes from(Scholarship s, String studentName, String deptName, Integer academicYear) {
        return ScholarshipRes.builder()
                .memberCode(s.getStudentCode())
                .studentName(studentName)
                .deptName(deptName)
                .academicYear(academicYear)
                .year(s.getYear())
                .semester(s.getSemester())
                .scholarshipType(s.getScholarshipType().getScholarshipType())
                .scholarshipAmount(s.getScholarshipAmount())
                .createdAt(s.getCreatedAt())
                .build();
    }
}