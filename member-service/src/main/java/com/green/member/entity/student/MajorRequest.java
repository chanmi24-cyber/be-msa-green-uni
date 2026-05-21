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

    @Column(name = "current_major_id", nullable = false)
    private Long currentMajorId;

    @Column(name = "current_minor_id")
    private Long currentMinorId;

    @Column(name = "target_major_id", nullable = false)
    private Long targetMajorId;

    @Column(name = "reason")
    private String reason;

    @Column(name = "file")
    private String file;

    @Column(name = "academic_year", nullable = false)
    private Integer academicYear;

    @Column(name = "semester", nullable = false)
    private Integer semester;

    @Column(name = "original_file_name")
    private String originalFileName;

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

    @Column(name = "updater_code")
    private Long updaterCode;

    public void setFile(String file){ this.file = file; }
    public void updateGpa(BigDecimal gpa) { this.gpa = gpa; }
    public void cancel() { this.status = EnumApprovalStatus.CANCELLED; }

    // 신청서 승인
    public void approve(String approveReason, Long updatorCode) {
        this.status = EnumApprovalStatus.APPROVED;
        this.approveReason = approveReason;
        this.updaterCode = updatorCode;
    }
    // 신청서 반려
    public void reject(String rejectReason, Long updatorCode) {
        this.status = EnumApprovalStatus.REJECTED;
        this.rejectReason = rejectReason;
        this.updaterCode = updatorCode;
    }
}