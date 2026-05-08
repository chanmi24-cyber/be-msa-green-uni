package com.green.member.application.student.model;

import com.green.common.enumcode.EnumStudentStatus;
import com.green.member.application.member.model.MemberProfileRes;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
public class StudentProfileRes extends MemberProfileRes {
    private String mainMajorName;
    private String subMajorName;
    private String collegeName;
    private Integer academicYear;
    private Integer semester;
    private Boolean isTransfer;
    private Boolean isMultiChild;
    private Boolean isVeteran;
    private EnumStudentStatus status;
}
