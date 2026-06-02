package com.green.member.application.status.model;

import java.time.LocalDateTime;

public interface StudentStatusRequestListRes {
    Long getRequestId();
    String getType();
    String getStatus();
    Integer getAcademicYear();
    Integer getSemester();
    Integer getReturnYear();
    Integer getReturnSemester();
    LocalDateTime getCreatedAt();
}
