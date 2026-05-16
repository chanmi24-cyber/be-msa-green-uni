package com.green.core.application.lecture.model;

import com.green.common.enumcode.EnumApprovalStatus;
import com.green.core.enumcode.EnumLectureType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureDetailRes {
    private Long memberCode;
    private Long lectureId;
    private Long majorId;
    private String majorName;
    private String proName;
    private Integer year;
    private Integer semester;
    private String lectureName;
    private Integer credit;
    private EnumLectureType lectureType;
    private String refBooks;
    private String goal;
    private String weeklyPlan;
    private Integer academicYear;
    private Integer maxStd;       // 학생은 null
    private EnumApprovalStatus status;  // 학생은 null
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<ScheduleRes> schedules;
    private String rejectionReason;
    private LocalDateTime rejectionAt;// 반려사유 (REJECTED일 때만 값 있음)

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