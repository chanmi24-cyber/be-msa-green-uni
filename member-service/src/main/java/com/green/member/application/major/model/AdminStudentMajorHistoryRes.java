package com.green.member.application.major.model;

import java.time.LocalDateTime;

public interface AdminStudentMajorHistoryRes {
    String getType();
    String getBeforeName();
    String getAfterName();
    Integer getAcademicYear();
    Integer getSemester();
    String getUpdaterName();
    LocalDateTime getUpdatedAt();
}
