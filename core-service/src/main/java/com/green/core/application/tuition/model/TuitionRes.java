package com.green.core.application.tuition.model;

import com.green.core.entity.tuition.Tuition;
import com.green.core.entity.tuition.TuitionPolicy;
import com.green.core.enumcode.EnumTuitionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class TuitionRes {
    private final Long tuitionId;
    private final Long studentCode;
    private final Integer year;
    private final Integer semester;
    private final String collegeName;
    private final Long baseAmount;
    private final Long totalDiscount;
    private final Long finalAmount;
    private final EnumTuitionStatus status;
    private final LocalDateTime paidAt;
    private final LocalDateTime deadline;

    public TuitionRes(Tuition tuition) {
        this.tuitionId = tuition.getTuitionId();
        this.studentCode = tuition.getStudentCode();
        this.year = tuition.getYear();
        this.semester = tuition.getSemester();
        this.collegeName = tuition.getTuitionPolicy().getCollege().getName();
        this.baseAmount = tuition.getBaseAmount();
        this.totalDiscount = tuition.getTotalDiscount();
        this.finalAmount = tuition.getFinalAmount();
        this.status = tuition.getStatus();
        this.paidAt = tuition.getPaidAt();
        this.deadline = tuition.getDeadline();
    }

    // API-TUI-03: 학생 납부 상세 조회 전용 응답 스펙 (안내 문구 및 계좌번호 추가)
    @Getter
    public static class MyTuitionDetailRes {
        private final Long tuitionId;
        private final Long baseAmount;
        private final Long totalDiscount;
        private final Long finalAmount;
        private final EnumTuitionStatus status;
        private final LocalDateTime deadline;
        private final String virtualAccount;
        private final String message;

        public MyTuitionDetailRes(Tuition tuition) {
            this.tuitionId = tuition.getTuitionId();
            this.baseAmount = tuition.getBaseAmount();
            this.totalDiscount = tuition.getTotalDiscount();
            this.finalAmount = tuition.getFinalAmount();
            this.status = tuition.getStatus();
            this.deadline = tuition.getDeadline();
            this.virtualAccount = "그린은행 123-456-789012 (예금주: 그린대학교)";

            // 명세서 기재 조건: unpaid, pending, paid 상태별 문구 분기
            this.message = switch (tuition.getStatus()) {
                case UNPAID -> "등록금 납부 금액을 확인하시고 기한 내에 납부해 주세요.";
                case PENDING -> "납부 확인중입니다.";
                case PAID -> "납부 완료했습니다.";
            };
        }
    }

    // API-TUI-05: 미납자 독촉 메일 미리보기 전용 응답 스펙
    @Getter
    @Builder
    @AllArgsConstructor
    public static class TuitionRemindRes {
        private final Integer unpaidCount;
        private final Integer year;
        private final Integer semester;
        private final LocalDateTime dueDate;
        private final String mailSubject;
        private final String mailFrom;
        private final String mailBodyPreview;
    }

    // API-TUI-11, 12: 등록금 정책 전체 조회/수정 전용 응답 스펙
    @Getter
    public static class PolicyRes {
        private final Long policyId;
        private final Integer year;
        private final Integer semester;
        private final Long collegeId;
        private final String collegeName;
        private final Long baseAmount;
        private final LocalDateTime updatedAt;
        private final String updatedBy;

        public PolicyRes(TuitionPolicy policy) {
            this.policyId = policy.getPolicyId();
            this.year = policy.getYear();
            this.semester = policy.getSemester();
            this.collegeId = policy.getCollege().getCollegeId();
            this.collegeName = policy.getCollege().getName();
            this.baseAmount = policy.getBaseAmount();
            this.updatedAt = policy.getUpdatedAt(); // 부모 엔티티 CreatedUpdatedAt에서 인스턴스 자동 상속
            this.updatedBy = policy.getUpdatorCode() != null ? String.valueOf(policy.getUpdatorCode()) : "SYSTEM";
        }
    }
}