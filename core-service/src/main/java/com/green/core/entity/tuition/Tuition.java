package com.green.core.entity.tuition;
import com.green.common.entity.CreatedAt;
import com.green.core.enumcode.EnumTuitionStatus;
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

    @Column(name = "updator_code", nullable = false)
    private Long updatorCode;

    @Column(name = "deadline", nullable = false)
    private LocalDateTime deadline;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EnumTuitionStatus status = EnumTuitionStatus.UNPAID;
}
