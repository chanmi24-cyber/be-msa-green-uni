package com.green.core.application.lecture.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// LEC-06, 07 공용
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyLectureListReq {
    private Long lectureId;
    private String lectureName;
    private Integer year;
    private Integer semester;
    private Integer page;
    private Integer size;
    private Integer startIdx;
}