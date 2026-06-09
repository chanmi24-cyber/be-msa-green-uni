package com.green.member.application.status.model;

import java.time.LocalDateTime;

public interface AdminStatusRequestListRes {
    Long getRequestId();
    Long getMemberCode();
    String getStudentName();
    String getUpdaterName();
    String getUpdaterCode();
    Integer getAcademicYear();
    Integer getSemester();
    String getType();
    String getStatus();
    LocalDateTime getCreatedAt();
}
