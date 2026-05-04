package com.green.core.entity.attendance;

import com.green.common.entity.CreatedUpdatedAt;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;
import com.green.core.entity.lecture.Lecture;

import java.time.LocalDate;

@Entity
@Table(name = "attendance_cancel",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_attendance_cancel",
                columnNames = {"lecture_id", "cancel_date"}  // 중복 방지는 UNIQUE로
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AttendanceCancel extends CreatedUpdatedAt {

    @Id @Tsid
    @Column(name = "cancel_id")
    private Long cancelId;  // 대리키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @Column(name = "cancel_date", nullable = false)
    private LocalDate cancelDate;

    @Column(name = "makeup_date")
    private LocalDate makeupDate;
}