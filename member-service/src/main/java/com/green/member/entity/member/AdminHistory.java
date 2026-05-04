package com.green.member.entity.member;

import com.green.common.entity.CreatedAt;
import com.green.member.enumcode.EnumAdminStatus;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "admin_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AdminHistory extends CreatedAt {

    @Id @Tsid
    @Column(name = "history_id", nullable = false)
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_code", nullable = false)
    private Admin admin;

    @Column(name = "change_type", nullable = false, length = 20)
    private String changeType; //휴직, 복직

    @Column(name = "old_status", nullable = false, length = 20)
    private EnumAdminStatus oldStatus;

    @Column(name = "new_status", nullable = false, length = 20)
    private EnumAdminStatus newStatus;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "reason")
    private String reason;

    @Column(name = "updator_code")
    private Long updatorCode;
}