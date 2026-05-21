package com.green.member.application.student.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface MajorRequestDetailRes {
    Long getRequestId();
    String getType();
    String getTargetMajorName();
    String getStatus();
    BigDecimal getGpa();
    String getReason();
    String getFile();
    String getOriginalFileName();
    String getApproveReason();
    String getRejectReason();
    Integer getAcademicYear();
    Integer getSemester();
    String getCurrentMajorName();
    String getCurrentMinorName();
    LocalDateTime getCreatedAt();
}
