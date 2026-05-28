package com.green.member.entity.member;

import com.green.common.entity.CreatedAt;
import com.green.common.enumcode.EnumChangeType;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MemberHistory extends CreatedAt {

    @Id @Tsid
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_code", nullable = false)
    private Member member;

    @Column(name = "change_type", nullable = false, length = 20)
    private EnumChangeType changeType;

    @Column(name = "before_data", nullable = false, columnDefinition = "JSON")
    private String beforeData;

    @Column(name = "updater_code", nullable = false)
    private Long updaterCode;
}