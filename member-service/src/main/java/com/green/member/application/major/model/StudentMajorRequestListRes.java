package com.green.member.application.major.model;

import java.time.LocalDateTime;

public interface StudentMajorRequestListRes {
    Long getRequestId();
    String getType();
    String getTargetMajorName();
    String getStatus();
    Integer getAcademicYear();
    Integer getSemester();
    LocalDateTime getCreatedAt();
}
