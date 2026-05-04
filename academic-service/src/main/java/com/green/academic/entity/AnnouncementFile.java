package com.green.academic.entity;

import com.green.common.entity.CreatedAt;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "announcement_file")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AnnouncementFile extends CreatedAt {

    @Id @Tsid
    @Column(name = "file_id")
    private Long fileId; // TSID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anno_id", nullable = false)
    private Announcement announcement;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "stored_name", nullable = false)
    private String storedName;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "file_ext", nullable = false, length = 10)
    private String fileExt;
}