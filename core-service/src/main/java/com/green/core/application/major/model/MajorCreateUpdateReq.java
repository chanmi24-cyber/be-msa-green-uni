package com.green.core.application.major.model;

import com.green.common.enumcode.EnumBuilding;
import com.green.core.enumcode.EnumMajorStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MajorCreateUpdateReq {
    private String name;
    private EnumBuilding majorBuilding;
    private String room;
    private String tel;
    private Long chairProfessorCode;
    private Integer capacity;
    private Integer courseDuration;
    private String foundedDate;
    private EnumMajorStatus active;
    private Long collegeId;
    private String info;
}