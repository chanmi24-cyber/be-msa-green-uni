package com.green.member.application.model;

import com.green.member.enumcode.EnumStudentStatus;
import lombok.Data;

@Data
public class StudentCreateReq {
    private Integer memberCode;
    private Long majorId;
    private Integer academicYear;
    private Integer semester;
    private Boolean isTransfer;
    private Boolean isMultiChild;
    private Boolean isVeteran;
    private EnumStudentStatus status;
}
