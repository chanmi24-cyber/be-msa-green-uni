package com.green.member.application.admin.model;

public interface StudentListDto {
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