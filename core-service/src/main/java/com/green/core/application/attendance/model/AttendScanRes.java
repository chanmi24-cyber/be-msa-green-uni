package com.green.core.application.attendance.model;

import com.green.core.enumcode.EnumAttendStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class AttendScanRes {
    private Long attendId;
    private EnumAttendStatus status;
    private LocalDate classDate;
}