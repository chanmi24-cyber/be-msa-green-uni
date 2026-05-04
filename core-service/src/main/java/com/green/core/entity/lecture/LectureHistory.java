package com.green.core.entity.lecture;
import com.green.common.entity.CreatedAt;
import com.green.common.enumcode.EnumChangeType;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "lecture_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LectureHistory extends CreatedAt {

    @Id @Tsid
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @Column(name = "change_type", nullable = false, length = 20)
    private EnumChangeType changeType;

    @Column(name = "before_data", nullable = false, columnDefinition = "JSON")
    private String beforeData;

    @Column(name = "change_reason", nullable = false, length = 100)
    private String changeReason;

    @Column(name = "updator_code", nullable = false)
    private Long updatorCode;

}