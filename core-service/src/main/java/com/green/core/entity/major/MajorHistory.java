package com.green.core.entity.major;
import com.green.common.entity.CreatedAt;
import com.green.common.enumcode.EnumChangeType;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "major_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MajorHistory extends CreatedAt {

    @Id @Tsid
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id", nullable = false)
    private Major major;

    @Column(name = "change_type", nullable = false, length = 20)
    private EnumChangeType changeType;

    @Column(name = "before_data", columnDefinition = "JSON", nullable = false)
    private String beforeData;

    @Column(name = "change_reason")
    private String changeReason;

    @Column(name = "updator_code", nullable = false)
    private Long updatorCode;

}