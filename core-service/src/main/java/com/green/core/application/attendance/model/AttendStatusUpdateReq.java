package com.green.core.application.attendance.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AttendStatusUpdateReq {
    private String status;
    private String reason;
}