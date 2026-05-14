package com.green.member.application.admin.model;

import com.green.common.enumcode.EnumProfessorDegree;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class AdminProfessorUpdateReq extends AdminMemberUpdateReq{
    private EnumProfessorDegree degree;
    private Long majorId;
}
