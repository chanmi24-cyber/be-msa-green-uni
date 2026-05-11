package com.green.member.application.professor.model;

import com.green.member.application.member.model.MemberProfileRes;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
public class ProfessorProfileRes extends MemberProfileRes {
    private String collegeName;
    private String majorName;
    private String degree;
    private String position;
    private String labBuilding;
    private String labRoom;
    private String labTel;
    private String status;
}
