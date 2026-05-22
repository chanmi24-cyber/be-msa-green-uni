package com.green.core.entity.tuition;

import com.green.common.entity.CreatedUpdatedAt;
import com.green.common.entity.UpdatedAt;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;
import com.green.core.entity.major.College;

@Entity
@Table(name = "tuition_policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TuitionPolicy extends UpdatedAt {

    @Id @Tsid
    @Column(name = "policy_id")
    private Long policyId;

    // 단과대당 하나의 정책만 존재하도록 유일성 보장 (Unique 제약조건)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id", nullable = false, unique = true)
    private College college;

    @Column(name = "base_amount", nullable = false)
    private Long baseAmount;

    @Column(name = "updator_code", nullable = false)
    private Long updatorCode;

    /**
     * 등록금 정책 금액 수정 및 수정자 기록
     */
    public void updateBaseAmount(Long baseAmount, Long updatorCode) {
        if (baseAmount == null || baseAmount <= 0) {
            throw new IllegalArgumentException("올바르지 않은 등록금 액수입니다.");
        }
        this.baseAmount = baseAmount;
        this.updatorCode = updatorCode;
    }
}