package com.green.member.application.status.model;

import com.green.common.enumcode.EnumApprovalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminStatusRequestProcessReq {
    @NotNull
    private EnumApprovalStatus status;
    private String rejectReason;
}
