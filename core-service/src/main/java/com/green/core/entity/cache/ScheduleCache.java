package com.green.core.entity.cache;
import com.green.common.enumcode.EnumScheduleType;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedule_cache")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ScheduleCache implements Persistable<Long> {

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

    @Transient  // DB 컬럼 아님
    private boolean isNew = true;  // 기본값 true (새 엔티티)

    @PostLoad  // DB에서 조회된 경우 false로
    void markNotNew() {
        this.isNew = false;
    }

    @Override
    public Long getId() { return scheduleId; }

    @Override
    public boolean isNew() { return isNew; }
}