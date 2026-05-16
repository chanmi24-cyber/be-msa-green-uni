package com.green.member.application.student.model;

import com.green.common.enumcode.EnumStudentStatus;
import lombok.Data;

@Data
public class StudentListRes {
    private Long memberCode;
    private String name;
    private String email;
    private String tel;
    private EnumStudentStatus status;
    private String collegeName;
    private String majorName;
    private String minorName;
    private Integer academicYear;
    private Integer semester;
}
