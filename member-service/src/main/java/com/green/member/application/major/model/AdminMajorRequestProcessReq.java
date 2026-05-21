package com.green.member.application.major.model;

import com.green.common.enumcode.EnumApprovalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminMajorRequestProcessReq {
    @NotNull
    private EnumApprovalStatus status; // APPROVED 또는 REJECTED만 허용
    private String rejectReason;
}
