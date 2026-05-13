package com.green.core.application.course.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseLectureRes {
    private Long lectureId;
    private String lectureName;
}
