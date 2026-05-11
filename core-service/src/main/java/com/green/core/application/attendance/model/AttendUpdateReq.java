package com.green.core.application.attendance.model;

import lombok.Getter;

@Getter
public class AttendUpdateReq {
    private String status;  // 영문 코드: ATTEND / ABSENT / LATE / EARLY_LEAVE
    private String reason;
}
