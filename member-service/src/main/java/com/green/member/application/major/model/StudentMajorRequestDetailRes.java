package com.green.member.application.major.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface StudentMajorRequestDetailRes {
    Long getRequestId();
    String getType();
    String getTargetMajorName();
    String getStatus();
    BigDecimal getGpa();
    String getReason();
    String getFile();
    String getOriginalFileName();
    String getRejectReason();
    Integer getAcademicYear();
    Integer getSemester();
    String getCurrentMajorName();
    String getCurrentMinorName();
    LocalDateTime getCreatedAt();
}
