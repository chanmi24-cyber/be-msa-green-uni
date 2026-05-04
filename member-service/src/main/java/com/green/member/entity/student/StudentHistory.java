package com.green.member.entity.student;

import com.green.common.entity.CreatedAt;
import com.green.common.enumcode.EnumStudentStatus;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "student_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StudentHistory extends CreatedAt {

    @Id @Tsid
    @Column(name = "history_id", nullable = false)
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_code", nullable = false)
    private Student student;

    @Column(name = "change_type", nullable = false, length = 20)
    private String changeType; // 신입학, 휴학, 복학, 자퇴

    @Column(name = "old_status", nullable = false, length = 20)
    private EnumStudentStatus oldStatus;

    @Column(name = "new_status", nullable = false, length = 20)
    private EnumStudentStatus newStatus;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    // 휴학 시에만
    @Column(name = "return_year")
    private Integer returnYear;

    // 휴학 시에만
    @Column(name = "return_semester")
    private Integer returnSemester;

    @Column(name = "reason")
    private String reason;

    @Column(name = "updator_code", nullable = false)
    private Long updatorCode;
}