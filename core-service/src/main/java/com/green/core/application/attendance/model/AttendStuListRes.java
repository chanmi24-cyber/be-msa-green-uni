package com.green.core.application.attendance.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

// API-ATTD-04: 학생 본인 출석 조회 응답
// 강의별로 출석 요약(총 횟수, 출석·결석·지각·조퇴 건수)과 세부 이력을 함께 반환
@Getter
@AllArgsConstructor
public class AttendStuListRes {
    private Long    lectureId;
    private String  lectureName;
    private Integer totalCount;
    private Integer attendCount;
    private Integer absentCount;
    private Integer lateCount;
    private Integer earlyLeaveCount;
    private List<Detail> details;

    @Getter
    @AllArgsConstructor
    public static class Detail {
        private String attendDate;   // "2026-04-01(수)" / "2026-05-01(수)(휴강)" / "(보강)" 포함 포맷
        private String status;       // ATTEND | ABSENT | LATE | EARLY_LEAVE | null(CANCEL 세션)
        private String reason;       // 사유 (교수 수동 입력 시 존재, 없으면 null)
        // [추가] QR 스캔 시각 — ATTEND·LATE 만 "HH:mm" 형식, 나머지 null
        private String attendedAt;
    }
}