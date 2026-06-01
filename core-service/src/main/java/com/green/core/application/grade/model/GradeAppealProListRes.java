package com.green.core.application.grade.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GradeAppealProListRes {
    private Long courseId;
    private Long studentCode;
    private String studentName;
    private Integer academicYear;
    private String lectureName;
    private Integer lectureYear;
    private Integer lectureSemester;
    private String reason;
    private String appealStatus;
    private LocalDateTime createdAt;
}
