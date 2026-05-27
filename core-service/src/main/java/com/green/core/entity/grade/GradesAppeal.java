package com.green.core.entity.grade;
import com.green.common.entity.CreatedUpdatedAt;
import com.green.core.enumcode.EnumAppealStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "grades_appeal")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GradesAppeal extends CreatedUpdatedAt {

    @Id
    @Column(name = "course_id")
    private Long courseId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId  // course_id를 grade의 PK와 연결, CASCADE없이 단순 FK만 연결
    @JoinColumn(name = "course_id")
    private Grade grade;

    @Column(name = "member_code", nullable = false)
    private Long memberCode;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "status", nullable = false, length = 10)
    @Builder.Default
    private EnumAppealStatus status = EnumAppealStatus.PENDING;

    @Column(name = "reject_reason", length = 200)
    private String rejectReason;

    // 교수 처리 시 백엔드에서 직접 입력
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    // [추가] 이의신청 내용 재신청 (REJECTED → PENDING)
    public void resubmit(String reason) {
        this.reason = reason;
        this.status = EnumAppealStatus.PENDING;
        this.rejectReason = null;
        this.processedAt = null;
    }

    // [추가] 교수 반려 처리
    public void reject(String rejectReason) {
        this.status = EnumAppealStatus.REJECTED;
        this.rejectReason = rejectReason;
        this.processedAt = LocalDateTime.now();
    }

    // [추가] 교수 승인 처리
    public void approve() {
        this.status = EnumAppealStatus.APPROVED;
        this.processedAt = LocalDateTime.now();
    }
}