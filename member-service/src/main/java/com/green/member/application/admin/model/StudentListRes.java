package com.green.member.application.admin.model;

import com.green.common.enumcode.EnumStudentStatus;
import lombok.Data;

@Data
public class StudentListRes {
    private Long memberCode;
    private String name;
    private String email;
    private String tel;
    private EnumStudentStatus status;
    private String college;
    private String majorName;
    private String minorName;
    private Integer academicYear;
    private Integer semester;
}
