package com.green.core.application.attendance.model;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class AttendSessionStartReq {
    private LocalDate classDate;
}