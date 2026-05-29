package com.green.academic.entity;

import com.green.academic.enumcode.EnumTargetRole;
import com.green.common.entity.CreatedUpdatedAt;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Entity
@Table(name = "announcement",
        indexes = @Index(
                name = "idx_announcement_deleted_at",
                columnList = "deleted_at"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Announcement extends CreatedUpdatedAt {

    @Id @Tsid
    @Column(name = "anno_id")
    private Long annoId;

    @Column(name = "member_code", nullable = false)
    private Long memberCode;

    @Column(name = "writer_name", nullable = false, length = 20)
    private String writerName;

    @Column(name = "target_role", nullable = false, length = 20)
    private EnumTargetRole targetRole; // STUDENT / PROFESSOR / ALL (JWT 없으면 ALL)

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "is_del", nullable = false)
    @Builder.Default
    private Boolean isDel = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void softDelete() {
        this.isDel = true;
        this.deletedAt = LocalDateTime.now();
    }

}
