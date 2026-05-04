package com.green.member.entity.student;

import com.green.common.entity.CreatedUpdatedAt;
import com.green.common.enumcode.EnumApprovalStatus;
import com.green.member.enumcode.EnumStatusRequestType;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "status_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StatusRequest extends CreatedUpdatedAt {

    @Id
    @Tsid
    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_code", nullable = false)
    private Student student;

    @Column(name = "type", nullable = false, length = 20)
    private EnumStatusRequestType type;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "file")
    private String file;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EnumApprovalStatus status = EnumApprovalStatus.PENDING;

    @Column(name = "approve_reason")
    private String approveReason;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "updator_code")
    private Long updatorCode;
}