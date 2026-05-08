package com.green.member.application.professor.model;

import com.green.common.enumcode.EnumBuilding;
import com.green.common.enumcode.EnumProfessorStatus;
import com.green.member.application.member.model.MemberProfileRes;
import com.green.member.enumcode.EnumProfessorDegree;
import com.green.member.enumcode.EnumProfessorPosition;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
public class ProfessorProfileRes extends MemberProfileRes {
    private String collegeName;
    private String majorName;
    private EnumProfessorDegree degree;
    private EnumProfessorPosition position;
    private EnumBuilding labBuilding;
    private String labRoom;
    private String labTel;
    private EnumProfessorStatus status;
}
