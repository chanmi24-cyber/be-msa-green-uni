package com.green.member.application.admin.model;

import com.green.common.enumcode.EnumAdminStatus;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@ToString
public class AdminHistoryRes {
    private String changeType;
    private EnumAdminStatus oldStatus;
    private EnumAdminStatus newStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LocalDateTime createdAt;
}
