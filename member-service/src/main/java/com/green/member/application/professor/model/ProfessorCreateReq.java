package com.green.member.application.professor.model;

import com.green.common.enumcode.EnumBuilding;
import com.green.common.enumcode.EnumProfessorStatus;
import com.green.member.application.member.model.MemberCreateReq;
import com.green.common.enumcode.EnumProfessorDegree;
import com.green.member.enumcode.EnumProfessorPosition;
import lombok.Data;

@Data
public class ProfessorCreateReq extends MemberCreateReq {
    private Long majorId;
    private EnumProfessorDegree degree;
    private EnumProfessorPosition position;
    private EnumBuilding labBuilding;
    private String labRoom;
    private String labTel;
    private EnumProfessorStatus status;
}
