package com.green.member.application.student.model;

import com.green.common.enumcode.EnumApprovalStatus;
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
    EnumApprovalStatus status;
    LocalDateTime createdAt;
    BigDecimal gpa;
    String reason;
    String file;
    String approveReason;
    String rejectReason;
}
