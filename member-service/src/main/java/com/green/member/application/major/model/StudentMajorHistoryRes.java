package com.green.member.application.major.model;

import java.time.LocalDateTime;

public interface StudentMajorHistoryRes {
    String getType();
    String getBeforeName();
    String getAfterName();
    Integer getAcademicYear();
    Integer getSemester();
    LocalDateTime getUpdatedAt();
}
