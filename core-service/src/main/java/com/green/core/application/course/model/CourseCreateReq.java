package com.green.core.application.course.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseCreateReq {
    @NotNull
    private Long lectureId;
}