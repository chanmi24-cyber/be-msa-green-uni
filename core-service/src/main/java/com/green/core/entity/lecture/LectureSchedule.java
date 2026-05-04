package com.green.core.entity.lecture;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "lecture_schedule",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"room_id", "day_of_week", "start_period", "lecture_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LectureSchedule {

    @Id
    @Tsid
    @Column(name = "schedule_id")
    private Long scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Classroom classRoom;

    @Column(name = "day_of_week", nullable = false, length = 5)
    private String dayOfWeek; // 월, 화, 수, 목, 금

    @Column(name = "start_period", nullable = false)
    private Integer startPeriod; // 1,2,3,4,5,6,7,8,9

    @Column(name = "end_period", nullable = false)
    private Integer endPeriod; // 1,2,3,4,5,6,7,8,9
}