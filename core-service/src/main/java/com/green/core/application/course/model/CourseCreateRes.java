package com.green.core.application.course.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseCreateRes {
    private Integer totalEnrolledCredits;
    private List<MyCourseRes> courses;
}