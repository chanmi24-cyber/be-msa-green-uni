package com.green.core.application.lecture.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// LEC-08 전체조회용
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureListReq {
    private Long lectureId;
    private String proName;
    private String lectureName;
    private Long majorId;
    private Integer year;
    private Integer semester;
    private Integer page;
    private Integer size;
    private Integer startIdx;
}