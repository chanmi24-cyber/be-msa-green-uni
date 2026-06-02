package com.green.core.entity.tuition;
import com.green.common.entity.CreatedAt;
import com.green.common.exception.BusinessException;
import com.green.core.enumcode.EnumTuitionStatus;
import com.green.core.exception.TuitionErrorCode;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tuition",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"student_code", "year", "semester"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Tuition extends CreatedAt {

    @Id @Tsid
    @Column(name = "tuition_id")
    private Long tuitionId;

    @Column(name = "student_code", nullable = false)
    private Long studentCode; // 복합 Unique

    @Column(name = "year", nullable = false)
    private Integer year; // 복합 Unique

    @Column(name = "semester", nullable = false)
    private Integer semester; // 복합 Unique

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private TuitionPolicy tuitionPolicy;

    @Column(name = "base_amount", nullable = false)
    private Long baseAmount;

    @Column(name = "total_discount", nullable = false)
    private Long totalDiscount;

    @Column(name = "final_amount", nullable = false)
    private Long finalAmount;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "updator_code")
    private Long updatorCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EnumTuitionStatus status = EnumTuitionStatus.UNPAID;

    @Column(name = "major_id", nullable = false)
    private Long majorId; // 고지서 해당 학기 당시의 학과 ID (★ 추가)

    // 전과 동기화 메서드도 majorId를 함께 변경하도록 수정
    public void updatePolicyAndBaseAmount(TuitionPolicy newPolicy, Long newBaseAmount, Long newFinalAmount, Long newMajorId) {
        if (this.status == EnumTuitionStatus.PAID) {
            throw new IllegalStateException("이미 등록금을 납부한 학생은 정책을 변경할 수 없습니다.");
        }
        this.tuitionPolicy = newPolicy;
        this.baseAmount = newBaseAmount;
        this.finalAmount = newFinalAmount;
        this.majorId = newMajorId; // 학과 ID 스냅샷 업데이트
    }

    public void requestPayment() {
        if (this.status == EnumTuitionStatus.PAID) {
            throw new BusinessException(TuitionErrorCode.ALREADY_PAID);
        }
        this.status = EnumTuitionStatus.PENDING;
    }

    public void completePayment(Long updatorCode) {
        this.status = EnumTuitionStatus.PAID;
        this.paidAt = LocalDateTime.now();
        this.updatorCode = updatorCode;
    }

    public void updateStatus(EnumTuitionStatus status, Long updatorCode) {
        this.status = status;
        this.updatorCode = updatorCode;
        if (status == EnumTuitionStatus.PAID) {
            this.paidAt = LocalDateTime.now();
        }
    }

    public void updateScholarshipDeduction(long totalDiscount, long finalAmount) {
        this.totalDiscount = totalDiscount;
        this.finalAmount = finalAmount;
    }

    /**
     * 전과로 인한 등록금 정책 변경 및 기본 금액 재책정
     */
    public void updatePolicyAndBaseAmount(TuitionPolicy newPolicy, Long newBaseAmount, Long newFinalAmount) {
        if (this.status == EnumTuitionStatus.PAID) {
            throw new IllegalStateException("이미 등록금을 납부한 학생은 정책을 변경할 수 없습니다.");
        }
        this.tuitionPolicy = newPolicy;
        this.baseAmount = newBaseAmount;
        this.finalAmount = newFinalAmount;
    }
}
