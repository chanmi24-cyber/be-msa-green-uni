package com.green.core.entity.attendance;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "qr_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class QrToken {

    @Id @Tsid
    @Column(name = "qrtoken_id")
    private Long qrtokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendsession_id", nullable = false)
    private AttendanceSession attendSession;

    @Column(name = "token", nullable = false, length = 100, unique = true)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}