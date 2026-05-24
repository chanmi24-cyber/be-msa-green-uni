package com.green.member.application.major.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface AdminMajorRequestDetailDto {
    Long getRequestId();
    Long getMemberCode();
    String getStudentName();
    String getPhone();
    String getEmail();
    String getAcademicStatus();
    String getTargetMajorName();
    String getCurrentMajorName();
    String getCurrentMinorName();
    String getType();
    String getStatus();
    BigDecimal getGpa();
    String getReason();
    String getFile();
    String getOriginalFileName();
    String getRejectReason();
    String getUpdaterName();
    Integer getAcademicYear();
    Integer getSemester();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}
