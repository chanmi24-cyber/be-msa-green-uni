package com.green.member.entity.student;

import com.green.common.entity.CreatedUpdatedAt;
import com.green.common.enumcode.EnumApprovalStatus;
import com.green.member.enumcode.EnumMajorRequestType;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "major_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MajorRequest extends CreatedUpdatedAt {

    @Id @Tsid
    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_code", nullable = false)
    private Student student;

    @Column(name = "type", nullable = false, length = 20)
    private EnumMajorRequestType type;

    @Column(name = "target_major_id", nullable = false)
    private Long targetMajorId;

    @Column(name = "reason")
    private String reason;

    @Column(name = "file")
    private String file;

    @Column(name = "gpa", nullable = false)
    @Digits(integer = 1, fraction = 2)
    private BigDecimal gpa;

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