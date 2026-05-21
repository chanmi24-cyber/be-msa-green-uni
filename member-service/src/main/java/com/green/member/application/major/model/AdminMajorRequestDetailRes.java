package com.green.member.application.major.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminMajorRequestDetailRes {
    Long requestId;
    Long memberCode;
    String studentName;
    String targetMajorName;
    String type;
    String status;
    BigDecimal gpa;
    String reason;
    String file;
    String originalFileName;
    String approveReason;
    String rejectReason;
    String updaterName;
    Integer academicYear;
    Integer semester;
    String currentMajorName;
    String currentMinorName;
    LocalDateTime createdAt;
}
