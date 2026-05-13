package com.green.core.application.attendance.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AttendStatusUpdateReq {
    private Long attendId; // 일괄 수정 시 어느 출석 레코드인지 식별
    private String status;
    private String reason;
}