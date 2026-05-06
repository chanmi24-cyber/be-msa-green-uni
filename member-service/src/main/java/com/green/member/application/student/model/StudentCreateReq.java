package com.green.member.application.student.model;

import com.green.common.enumcode.EnumStudentStatus;
import com.green.member.application.member.model.MemberCreateReq;
import lombok.Data;

@Data
public class StudentCreateReq extends MemberCreateReq {
    private Long majorId;
    private Integer academicYear;
    private Integer semester;
    private Boolean isTransfer;
    private Boolean isMultiChild;
    private Boolean isVeteran;
    private EnumStudentStatus status;
}
