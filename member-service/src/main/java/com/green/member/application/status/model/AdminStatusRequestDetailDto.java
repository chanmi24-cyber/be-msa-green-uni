package com.green.member.application.status.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface AdminStatusRequestDetailDto {
    Long getRequestId();
    Long getMemberCode();
    String getStudentName();
    String getType();
    String getStatus();
    String getReason();
    String getFile();
    String getOriginalFileName();
    String getRejectReason();
    String getUpdaterName();
    Integer getAcademicYear();
    Integer getSemester();
    Integer getReturnYear();
    Integer getReturnSemester();
    LocalDate getStartDate();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}
