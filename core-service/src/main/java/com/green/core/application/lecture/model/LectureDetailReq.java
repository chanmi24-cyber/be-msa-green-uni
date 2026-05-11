package com.green.core.application.lecture.model;

import com.green.core.enumcode.EnumLectureType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureDetailReq {
    private Long majorId;
    private Integer year;
    private Integer semester;
    private String lectureName;
    private Integer credit;
    private EnumLectureType lectureType;
    private String refBooks;
    private String goal;
    private String weeklyPlan;
    private Integer academicYear;
    private Integer maxStd;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<ScheduleReq> schedules;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScheduleReq {
        private Long roomId;
        private String dayOfWeek;
        private Integer startPeriod;
        private Integer endPeriod;
    }
}