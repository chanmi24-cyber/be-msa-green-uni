package com.green.member.application.professor.model;

import com.green.member.application.member.model.MemberCreateReq;
import com.green.member.enumcode.EnumProfessorDegree;
import com.green.member.enumcode.EnumProfessorPosition;
import lombok.Data;

@Data
public class ProfessorCreateReq extends MemberCreateReq {
    private Long majorId;
    private EnumProfessorDegree degree;
    private EnumProfessorPosition position;
    private String labRoom;
    private String labTel;
    private String status;
}
