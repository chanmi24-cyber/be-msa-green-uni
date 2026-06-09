package com.green.member.application.status.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface StudentStatusRequestDetailRes {
    Long getRequestId();
    String getType();
    String getStatus();
    String getReason();
    String getFile();
    String getOriginalFileName();
    String getRejectReason();
    Integer getAcademicYear();
    Integer getSemester();
    LocalDate getStartDate();
    Integer getReturnYear();
    Integer getReturnSemester();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}
