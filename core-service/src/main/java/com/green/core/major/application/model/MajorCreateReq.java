package com.green.core.major.application.model;
import com.green.core.major.enumcode.EnumMajorStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MajorCreateReq {
    private Integer majorCode;
    private String name;
    private EnumMajorStatus active;
    private String room;
    private String tel;
    private Integer professorCode;
    private Integer capacity;
}