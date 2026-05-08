package com.green.core.application.lecture.model;

import com.green.common.enumcode.EnumApprovalStatus;
import com.green.core.enumcode.EnumLectureType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyLectureListRes {//status포함(LEC-03, 06용)
        private Long lectureId;
        private Long memberCode;
        private EnumApprovalStatus status;
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
