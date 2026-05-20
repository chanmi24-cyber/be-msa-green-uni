package com.green.core.entity.attendance;

import com.green.core.entity.lecture.Lecture;
import com.green.core.enumcode.EnumSessionType;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

import java.time.*;

@Entity
@Table(name = "attendance_session",
        uniqueConstraints = @UniqueConstraint(columnNames = {"lecture_id", "class_date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AttendanceSession {

    @Id @Tsid
    @Column(name = "attendsession_id")
    private Long attendsessionId; // TSID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = false;

    @Column(name = "session_type", nullable = false, length = 20)
    @Builder.Default
    private EnumSessionType sessionType = EnumSessionType.NORMAL; // NORMAL/CANCEL/MAKEUP

    @Column(name = "class_date", nullable = false)
    private LocalDate classDate;

    @Column(name = "original_date")
    private LocalDate originalDate;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    // JPA에서 엔티티 값을 바꿀 때는 setter 대신 의미있는 메서드로 만드는 게 관례
    public void end() {
        this.isActive = false;
        this.endedAt = LocalDateTime.now();
    }
}