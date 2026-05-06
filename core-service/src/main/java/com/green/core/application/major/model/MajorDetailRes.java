package com.green.core.application.major.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class MajorDetailRes {
    private Long majorId;
    private String name;
    private String active;
    private String college;
    private String majorBuilding;
    private String room;
    private String tel;
    private Long professorCode;
    private String chairProfessorName;
    private Integer capacity;
    private LocalDate startDate;
    private String info;
}
