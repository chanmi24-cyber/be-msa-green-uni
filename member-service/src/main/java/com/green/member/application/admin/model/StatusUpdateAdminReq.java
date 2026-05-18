package com.green.member.application.admin.model;

import com.green.member.enumcode.EnumAdminStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class StatusUpdateAdminReq {
    @NotNull(message = "상태값은 필수입니다")
    private EnumAdminStatus status;
    private String reason;
    private LocalDate startDate;
    private LocalDate endDate;
}
