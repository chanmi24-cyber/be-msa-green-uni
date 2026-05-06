package com.green.core.application.major.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MajorRes {
    private Long majorId;
    private String name;
    private String building;
    private String room;
    private String tel;
    private Long professorCode;
    private String chairProfessorName;
    private Integer capacity;
    private Integer professorCount;
    private String college;
    private String active;
    private Integer totalPages;
}