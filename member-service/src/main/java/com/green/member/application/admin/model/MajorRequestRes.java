package com.green.member.application.admin.model;

import java.time.LocalDateTime;

public interface MajorRequestRes {
    Long getRequestId();
    Long getMemberCode();
    String getStudentName();
    String getTargetMajorName();
    String getType();
    String getStatus();
    LocalDateTime getCreatedAt();
}
