package com.green.core.application.attendance.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AttendLectureRes {
    private Long lectureId;
    private String lectureName;
    private Integer year;
    private Integer semester;
    private Integer credit;
    private String lectureType;
    private Integer academicYear;
    private List<ScheduleInfo> schedules;

    @Getter
    @AllArgsConstructor
    public static class ScheduleInfo {
        private String dayOfWeek;
        private Integer startPeriod;
        private Integer endPeriod;
        private String lectureRoom;
    }
}