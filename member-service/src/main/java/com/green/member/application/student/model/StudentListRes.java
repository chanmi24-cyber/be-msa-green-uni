package com.green.member.application.student.model;

public interface StudentListRes {
    Long getMemberCode();
    String getName();
    String getEmail();
    String getTel();
    String getStatus();
    Integer getAcademicYear();
    Integer getSemester();
    String getCollege();
    String getMajorName();
    String getMinorName();
}
