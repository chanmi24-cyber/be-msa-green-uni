package com.green.academic.entity;

import com.green.common.entity.CreatedAt;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification extends CreatedAt {

    @Id @Tsid
    @Column(name = "noti_id")
    private Long notiId;

    @Column(name = "member_code")
    private Long memberCode; // auth_member의 member_code 참조

    @Column(name = "target_role", length = 15)
    private String targetRole;

    @Column(name = "type", nullable = false, length = 100)
    private String type; // COURSE_REGISTRATION_START 등

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Column(name = "url", length = 1000)
    private String url;

    @Column(name = "ref_table_name", length = 15)
    private String refTableName; // @Enumerated 활용

    @Column(name = "ref_id", nullable = false)
    private Long refId;

    @Column(name = "is_notified", nullable = false)
    @Builder.Default
    private Boolean isNotified = false;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    public void read() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

}