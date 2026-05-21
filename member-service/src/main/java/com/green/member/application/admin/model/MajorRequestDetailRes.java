package com.green.member.application.admin.model;

import com.green.member.enumcode.EnumMajorRequestType;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ToString
@Builder
public class MajorRequestDetailRes {
    Long requestId;
    EnumMajorRequestType type;
    String targetMajorName;
    String status;
    LocalDateTime createdAt;
    BigDecimal gpa;
    String reason;
    String file;
    String originalFileName;
    String approveReason;
    String rejectReason;
}
