package com.green.core.entity.attendance;

import com.green.core.enumcode.EnumAttendStatus;
import com.green.common.entity.CreatedUpdatedAt;
import com.green.core.entity.course.Course;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attendance",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_attendance",
                columnNames = {"course_id", "attendsession_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Attendance extends CreatedUpdatedAt {

    @Id @Tsid
    @Column(name = "attend_id")
    private Long attendId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendsession_id", nullable = false)
    private AttendanceSession attendsession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course; // 복합 Unique

    @Column(name = "student_code", nullable = false)
    private Long studentCode; // JWT 토큰 사용

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EnumAttendStatus status = EnumAttendStatus.ATTEND; // ATTEND/ABSENT/LATE/EARLY_LEAVE

    @Column(name = "reason", length = 200)
    private String reason;
}