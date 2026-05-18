package com.green.member.application.major.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class MajorListRes {
    private Long majorId;
    private String name;
    private String collegeName;
}
