package com.green.auth.entity;

import com.green.common.entity.CreatedAt;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class RefreshToken extends CreatedAt {

    @Id @Tsid
    private Long tokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_code", nullable = false)
    private AuthMember authMember;

    @Column(nullable = false, unique = true, length = 512)
    private String tokenValue;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    public RefreshToken(AuthMember authMember, String tokenValue, LocalDateTime expiresAt) {
        this.authMember = authMember;
        this.tokenValue = tokenValue;
        this.expiresAt = expiresAt;
    }
}