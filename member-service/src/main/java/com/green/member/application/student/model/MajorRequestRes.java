package com.green.member.application.student.model;

import java.time.LocalDateTime;

public interface MajorRequestRes {
    Long getRequestId();
    String getType();
    String getTargetMajorName();
    String getStatus();
    Integer getAcademicYear();
    Integer getSemester();
    LocalDateTime getCreatedAt();
}
