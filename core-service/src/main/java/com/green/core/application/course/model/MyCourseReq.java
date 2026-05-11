package com.green.core.application.course.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyCourseReq {
    private int page = 1;
    private int size = 3;
}