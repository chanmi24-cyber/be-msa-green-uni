package com.green.member.application.admin.model;

import com.green.member.enumcode.EnumAdminStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class StatusUpdateAdminReq {
    private EnumAdminStatus status;
    private String reason;
    private LocalDate startDate;
    private LocalDate endDate;
}
