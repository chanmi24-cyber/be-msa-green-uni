package com.green.academic.entity;

import com.green.common.entity.CreatedUpdatedAt;
import com.green.common.enumcode.EnumScheduleType;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Schedule extends CreatedUpdatedAt {

    @Id @Tsid
    @Column(name = "schedule_id")
    private Long scheduleId; // TSID

    @Column(name = "member_code", nullable = false)
    private Long memberCode;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "semester", nullable = false)
    private Integer semester;

    @Column(name = "title", nullable = false, length = 30)
    private String title;

    @Column(name = "type", nullable = false, length = 30)
    private EnumScheduleType type;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = false;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "is_notified_start", nullable = false)
    @Builder.Default
    private Boolean isNotifiedStart = false;

    @Column(name = "is_notified_end", nullable = false)
    @Builder.Default
    private Boolean isNotifiedEnd = false;

    public void updateActive(boolean isActive) {
        this.isActive = isActive;
    }
}