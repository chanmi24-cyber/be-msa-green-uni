package com.green.member.application.admin.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    String updatorName;
    LocalDateTime createdAt;
    List<CurrentMajorDto> currentMajors;
}
