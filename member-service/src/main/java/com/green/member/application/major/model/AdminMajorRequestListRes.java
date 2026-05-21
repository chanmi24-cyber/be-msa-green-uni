package com.green.member.application.major.model;

import java.time.LocalDateTime;

public interface AdminMajorRequestListRes {
    Long getRequestId();
    Long getMemberCode();
    String getStudentName();
    String getTargetMajorName();
    String getType();
    String getStatus();
    LocalDateTime getCreatedAt();
}
