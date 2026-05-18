package com.green.core.entity.tuition;
import com.green.common.entity.CreatedUpdatedAt;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;
import com.green.core.entity.major.College;

@Entity
@Table(name = "tuition_policy",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"year", "semester", "college_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TuitionPolicy extends CreatedUpdatedAt {

    @Id @Tsid
    @Column(name = "policy_id")
    private Long policyId;

    @Column(name = "year", nullable = false)
    private Integer year; // 복합 Unique

    @Column(name = "semester", nullable = false)
    private Integer semester; // 복합 Unique

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id", nullable = false)
    private College college; // 복합 Unique

    @Column(name = "base_amount", nullable = false)
    private Long baseAmount;

    @Column(name = "updator_code", nullable = false)
    private Long updatorCode;

    public void updateBaseAmount(Long baseAmount, Long updatorCode) {
        this.baseAmount = baseAmount;
        this.updatorCode = updatorCode;
    }
}
