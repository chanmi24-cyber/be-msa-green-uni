package com.green.member.entity;

import com.green.common.entity.UpdatedAt;
import com.green.member.enumcode.EnumAdminStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admin")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Admin extends UpdatedAt {

    @Id
    @Column(name = "member_code")
    private Long memberCode;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_code", nullable = false)
    private Member member;

    @Column(name = "status", nullable = false, length = 10)
    @Builder.Default
    private EnumAdminStatus status = EnumAdminStatus.EMPLOYMENT;

}