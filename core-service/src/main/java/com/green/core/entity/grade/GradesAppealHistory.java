package com.green.core.entity.grade;

import com.green.common.entity.CreatedAt;
import com.green.core.enumcode.EnumAppealStatus;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "grades_appeal_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GradesAppealHistory extends CreatedAt {

    @Id @Tsid
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "member_code", nullable = false)
    private Long memberCode;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "status", nullable = false, length = 10)
    private EnumAppealStatus status;

    @Column(name = "reject_reason", length = 200)
    private String rejectReason;
}
