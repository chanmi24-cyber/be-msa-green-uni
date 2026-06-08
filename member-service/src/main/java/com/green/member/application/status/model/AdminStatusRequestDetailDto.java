package com.green.member.application.status.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface AdminStatusRequestDetailDto {
    Long getRequestId();
    Long getMemberCode();
    String getStudentName();
    String getPhone();
    String getEmail();
    String getType();
    String getStatus();
    String getReason();
    String getFile();
    String getOriginalFileName();
    String getRejectReason();
    String getUpdaterName();
    Long getUpdaterCode();
    Integer getAcademicYear();
    Integer getSemester();
    Integer getReturnYear();
    Integer getReturnSemester();
    LocalDate getStartDate();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
    Integer getTotalCredits();
    String getAcademicStatus();
    String getCurrentMajorName();
    String getCurrentMinorName();
}
