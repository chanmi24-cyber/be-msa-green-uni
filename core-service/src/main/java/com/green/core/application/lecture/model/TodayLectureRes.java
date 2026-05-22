package com.green.core.application.lecture.model;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodayLectureRes {
    private Long lectureId;
    private String lectureName;
    private String building;
    private String room;
    private Integer startPeriod;
    private Integer endPeriod;
    private Long attendSessionId;
    private Boolean isActive;
}