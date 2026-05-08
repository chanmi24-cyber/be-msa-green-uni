package com.green.core.entity.lecture;

import com.green.common.entity.CreatedUpdatedAt;
import com.green.common.enumcode.EnumApprovalStatus;
import com.green.core.entity.major.Major;
import com.green.core.enumcode.EnumLectureType;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

import java.time.*;
@Entity
@Table(name = "lecture")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Lecture extends CreatedUpdatedAt {

    @Id @Tsid
    @Column(name = "lecture_id")
    private Long lectureId;

    @Column(name = "member_code", nullable = false)
    private Long memberCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id", nullable = false)
    private Major major;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "semester", nullable = false)
    private Integer semester;

    @Column(name = "lecture_name", nullable = false, length = 30)
    private String lectureName;

    @Column(name = "credit", nullable = false)
    private Integer credit;

    @Column(name = "lecture_type", nullable = false, length = 20)
    private EnumLectureType lectureType;

    @Column(name = "ref_books", nullable = false, length = 1000)
    private String refBooks;

    @Column(name = "goal", nullable = false, length = 500)
    private String goal;

    @Column(name = "weekly_plan", nullable = false, columnDefinition = "TEXT")
    private String weeklyPlan;

    @Column(name = "academic_year", nullable = false)
    private Integer academicYear;

    @Column(name = "max_std", nullable = false)
    private Integer maxStd;

    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private EnumApprovalStatus status = EnumApprovalStatus.PENDING;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "is_del", nullable = false)
    @Builder.Default
    private Boolean isDel = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        if (this.year == null) {
            this.year = LocalDate.now().getYear();
        }
    }

    public void updateStatus(EnumApprovalStatus status) {
        this.status = status;
    }
}