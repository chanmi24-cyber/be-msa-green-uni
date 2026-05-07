package com.green.core.application.major.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CollegeListRes {
    private Long collegeId;
    private String name;
}