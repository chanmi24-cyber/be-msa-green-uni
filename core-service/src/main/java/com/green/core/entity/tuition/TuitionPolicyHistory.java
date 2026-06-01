package com.green.core.entity.tuition;

import com.green.common.entity.CreatedAt;
import com.green.common.enumcode.EnumChangeType;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tuition_policy_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TuitionPolicyHistory extends CreatedAt {

    @Id @Tsid
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private TuitionPolicy tuitionPolicy;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 20)
    private EnumChangeType changeType; // 예: WRITE, UPDATE 등

    // 이전 정책의 스냅샷 데이터를 JSON 문자열(예: {"baseAmount": 3000000})로 저장
    @Column(name = "before_data", columnDefinition = "JSON", nullable = false)
    private String beforeData;

    @Column(name = "change_reason")
    private String changeReason;

    @Column(name = "updator_code", nullable = false)
    private Long updatorCode;

    @Column(name = "target_year", nullable = false)
    private Integer targetYear; // 예: 2025

    @Column(name = "target_semester", nullable = false)
    private Integer targetSemester; // 예: 1
}