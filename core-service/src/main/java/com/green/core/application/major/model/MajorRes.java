package com.green.core.application.major.model;

import com.green.common.enumcode.EnumBuilding;
import com.green.core.enumcode.EnumMajorStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MajorRes {
    private Long majorId;
    private String name;
    private EnumBuilding majorBuilding;
    private String room;
    private String tel;
    private Long professorCode;
    private Integer capacity;
//    private Integer professorCount;
    private Long collegeId;
    private EnumMajorStatus active;
//    private Integer totalPages;
}