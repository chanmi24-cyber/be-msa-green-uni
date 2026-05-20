package com.green.core.application.tuition.model;

import com.green.core.entity.cache.StudentCache;
import com.green.core.entity.tuition.Tuition;
import com.green.core.entity.tuition.TuitionPolicy;
import com.green.core.entity.tuition.TuitionPolicyHistory;
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
    private final String studentName;
    private final String deptName;
    private final Integer academicYear;
    private final String collegeName;
    private final Long baseAmount;
    private final Long totalDiscount;
    private final Long finalAmount;
    private final EnumTuitionStatus status;
    private final LocalDateTime paidAt;
    private final LocalDateTime deadline;

    //만약 TuitionRes에 파라미터가 3개인 생성자만 있다면, TuitionRes::new가 호출할 수 있는 Tuition만 받는 생성자가 존재하지 않습니다. 따라서 추가 필요.
    public TuitionRes(Tuition t) {
        this(t, null, "학과 정보 없음");
    }

    // 학생 정보까지 포함하는 생성자
    public TuitionRes(Tuition tuition, StudentCache student, String majorName) {
        this.tuitionId = tuition.getTuitionId();
        this.studentCode = tuition.getStudentCode();
        this.year = tuition.getYear();
        this.semester = tuition.getSemester();

        this.studentName = (student != null) ? student.getName() : null;
        this.deptName = majorName; // 학과ID 대신 실제 이름 할당
        this.academicYear = (student != null) ? student.getAcademicYear() : null;

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
    // TuitionRes 클래스 내부의 PolicyRes 스펙 가이드라인 예시
    @Getter
    public static class PolicyRes {
        private Long policyId;
        private Long collegeId;
        private String collegeName;
        private Long baseAmount;
        private Long updatedBy;
        private LocalDateTime updatedAt;

        public PolicyRes(TuitionPolicy policy) {
            this.policyId = policy.getPolicyId();
            this.baseAmount = policy.getBaseAmount();
            this.updatedBy = policy.getUpdatorCode();
            this.updatedAt = policy.getUpdatedAt(); // CreatedUpdatedAt 상속분 사용
            if (policy.getCollege() != null) {
                this.collegeId = policy.getCollege().getCollegeId();
                this.collegeName = policy.getCollege().getName(); // 단과대학 명칭 매핑
            }
        }
    }

    @Getter
    public static class PolicyHistoryRes {
        private Long policyId;
        private String collegeName;
        private Long baseAmount;
        private LocalDateTime createdAt;
        private Long updatorCode;

        // 파싱된 amount를 받는 생성자
        public PolicyHistoryRes(TuitionPolicyHistory history, Long baseAmount) {
            this.policyId = history.getTuitionPolicy().getPolicyId();
            this.collegeName = history.getTuitionPolicy().getCollege().getName();
            this.baseAmount = baseAmount; // 파싱된 금액 할당
            this.createdAt = history.getCreatedAt();
            this.updatorCode = history.getUpdatorCode();
        }
    }
}