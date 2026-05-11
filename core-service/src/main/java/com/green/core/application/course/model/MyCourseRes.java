package com.green.core.application.course.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyCourseRes {
    private Long lectureId;
    private String majorName;
    private String lectureName;
    private String building;
    private String roomNumber;
    private String lectureType;
    private Integer academicYear;
    private String proName;
    private String dayOfWeek;
    private Integer startPeriod;
    private Integer endPeriod;
    private Integer credit;
    private Integer maxStd;
    private Integer remStd;
    private String status;
    private Integer isAttended;
}