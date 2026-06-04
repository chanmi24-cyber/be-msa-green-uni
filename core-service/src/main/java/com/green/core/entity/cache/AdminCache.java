package com.green.core.entity.cache;

import com.green.common.enumcode.EnumAdminStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admin_cache")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AdminCache {

    @Id
    @Column(name = "member_code")
    private Long memberCode;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "status", nullable = false, length = 20)
    private EnumAdminStatus status;
}