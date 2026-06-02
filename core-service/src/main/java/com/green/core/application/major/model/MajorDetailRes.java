package com.green.core.application.major.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.green.common.enumcode.EnumBuilding;
import com.green.core.enumcode.EnumMajorStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class MajorDetailRes {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long majorId;
    private String name;
    private EnumMajorStatus active;
    private String college;
    private EnumBuilding majorBuilding;
    private String room;
    private String tel;
    private Long professorCode;
    private Integer capacity;
    private Integer courseDuration;
    private String foundedDate;
    private String info;
    private String closedDate;
}
