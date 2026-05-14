package com.green.member.entity.professor;
import com.green.common.entity.CreatedAt;
import com.green.member.enumcode.NullableProfessorStatusConverter;
import com.green.member.enumcode.EnumProfessorPosition;
import com.green.common.enumcode.EnumProfessorStatus;
import com.green.member.enumcode.NullableProfessorPositionConverter;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "professor_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProfessorHistory extends CreatedAt {

    @Id @Tsid
    @Column(name = "history_id", nullable = false)
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_code", nullable = false)
    private Professor professor;

    //신규임용,휴직,복직,안식년,안식년종료,퇴임,직위변경
    @Column(name = "change_type", nullable = false, length = 20)
    private String changeType;

    @Convert(converter = NullableProfessorStatusConverter.class)
    @Column(name = "old_status", length = 20)
    private EnumProfessorStatus oldStatus;

    @Convert(converter = NullableProfessorStatusConverter.class)
    @Column(name = "new_status", length = 20)
    private EnumProfessorStatus newStatus;

    // 전임교수, 시간강사, 조교수, 명예교수
    @Convert(converter = NullableProfessorPositionConverter.class)
    @Column(name = "old_position", length = 20)
    private EnumProfessorPosition oldPosition;

    @Convert(converter = NullableProfessorPositionConverter.class)
    @Column(name = "new_position", length = 20)
    private EnumProfessorPosition newPosition;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "reason")
    private String reason;

    @Column(name = "updator_code", nullable = false)
    private Long updatorCode;
}