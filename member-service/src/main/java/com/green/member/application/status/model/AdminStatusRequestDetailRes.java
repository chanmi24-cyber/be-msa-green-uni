package com.green.member.application.status.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminStatusRequestDetailRes {
    Long requestId;
    Long memberCode;
    String studentName;
    String phone;
    String email;
    String currentMajorName;
    String currentMinorName;
    String academicStatus;
    String type;
    String status;
    String reason;
    String file;
    String originalFileName;
    String rejectReason;
    String updaterName;
    Long updaterCode;
    Integer academicYear;
    Integer semester;
    Integer returnYear;
    Integer returnSemester;
    LocalDate startDate;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    Integer totalCredits; // core-service 연동 후 채울 필드
}
