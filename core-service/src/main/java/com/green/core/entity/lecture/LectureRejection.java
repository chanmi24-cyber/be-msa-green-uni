package com.green.core.entity.lecture;
import com.green.common.entity.CreatedAt;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "lecture_rejection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LectureRejection extends CreatedAt {

    @Id @Tsid
    @Column(name = "rejection_id")
    private Long rejectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    @Column(name = "updator_code", nullable = false)
    private Long updatorCode;
}