package com.green.member.application.student.model;

import com.green.common.enumcode.EnumStudentStatus;
import lombok.Getter;

@Getter
public class StudentListReq {
    private Integer page;
    private Integer size;
    private EnumStudentStatus status;
    private Long collegeId;
    private Integer academicYear;
    private String majorName;
    private String name;
}
