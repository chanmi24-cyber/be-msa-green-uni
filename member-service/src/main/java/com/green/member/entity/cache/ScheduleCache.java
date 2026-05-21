package com.green.member.entity.cache;
import com.green.common.enumcode.EnumScheduleType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedule_cache")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ScheduleCache {

    @Id
    @Column(name = "schedule_id")
    private Long scheduleId;

    @Convert(converter = EnumScheduleType.CodeConverter.class)
    @Column(name = "type", nullable = false, length = 30)
    private EnumScheduleType type;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "semester", nullable = false)
    private Integer semester;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}