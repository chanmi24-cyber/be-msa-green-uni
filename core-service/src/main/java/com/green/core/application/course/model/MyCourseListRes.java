package com.green.core.application.course.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

// MyCourseListRes.java 새로 생성
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyCourseListRes {
    private int totalEnrolledCredits;
    private List<MyCourseRes> courses;
}
