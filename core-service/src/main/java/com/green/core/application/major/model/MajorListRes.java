package com.green.core.application.major.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MajorListRes {
    private Long majorId;
    private String majorName;
}