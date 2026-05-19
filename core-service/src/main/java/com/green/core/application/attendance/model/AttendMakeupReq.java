package com.green.core.application.attendance.model;

import lombok.Getter;

import java.time.LocalDate;

// [추가] API-ATTD-09: 보강 세션 시작 요청
@Getter
public class AttendMakeupReq {
    private LocalDate classDate;    // 보강 실시 날짜 (오늘)
    private LocalDate originalDate; // 보강 대상인 원래 휴강 날짜
}