package com.green.member.application.admin.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface MajorRequestDetailDto {
    Long getRequestId();
    Long getMemberCode();
    String getStudentName();
    String getTargetMajorName();
    String getType();
    String getStatus();
    BigDecimal getGpa();
    String getReason();
    String getFile();
    String getOriginalFileName();
    String getApproveReason();
    String getRejectReason();
    String getUpdatorName();
    LocalDateTime getCreatedAt();
}
