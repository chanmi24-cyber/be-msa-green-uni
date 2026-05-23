package com.green.member.entity.student;

import com.green.common.entity.CreatedUpdatedAt;
import com.green.common.enumcode.EnumApprovalStatus;
import com.green.member.enumcode.EnumStatusRequestType;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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

    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "academic_year")
    private Integer academicYear;

    @Column(name = "semester")
    private Integer semester;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "return_year")
    private Integer returnYear;

    @Column(name = "return_semester")
    private Integer returnSemester;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EnumApprovalStatus status = EnumApprovalStatus.PENDING;

    @Column(name = "note")
    private String note;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "updater_code")
    private Long updaterCode;

    public void setFile(String file) { this.file = file; }
    public void cancel() { this.status = EnumApprovalStatus.CANCELLED; }

    public void approve(String note, Long updaterCode) {
        this.status = EnumApprovalStatus.APPROVED;
        this.note = note;
        this.updaterCode = updaterCode;
    }

    public void reject(String rejectReason, Long updaterCode) {
        this.status = EnumApprovalStatus.REJECTED;
        this.rejectReason = rejectReason;
        this.updaterCode = updaterCode;
    }
}