package com.green.core.application.attendance.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

// [추가] API-ATTD-10: 휴강 내역 조회 응답 (보강 모달 드롭다운용)
@Getter
@AllArgsConstructor
public class AttendCancelHistoryRes {
    private LocalDate cancelDate;
    private LocalDate makeupDate; // 보강 완료 시 날짜, 미완료 시 null
}