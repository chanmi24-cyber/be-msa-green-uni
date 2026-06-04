package com.green.core.application.attendance.model;

import lombok.Getter;

import java.time.LocalDate;

// [추가] AttendSessionStartReq에서 이름 변경 — 세션 시작·휴강 처리 공용
@Getter
public class AttendSessionReq {
    private LocalDate classDate;
}