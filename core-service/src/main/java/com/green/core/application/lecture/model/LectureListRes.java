package com.green.core.application.lecture.model;

import com.green.core.enumcode.EnumLectureType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

// LEC-07, 08용
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureListRes {
    private Long lectureId;
    private Long memberCode;
    private EnumLectureType lectureType;
    private String lectureName;
    private String majorName;
    private String proName;
    private Integer credit;
    private Integer academicYear;
    private Integer year;
    private Integer semester;
    private Integer totalCount;
    private List<ScheduleRes> schedules;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScheduleRes {
        private String dayOfWeek;
        private Integer startPeriod;
        private Integer endPeriod;
        private String building;
        private String room;
    }
}