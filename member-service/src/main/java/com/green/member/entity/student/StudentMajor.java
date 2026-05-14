package com.green.member.entity.student;

import com.green.common.entity.CreatedUpdatedAt;
import com.green.common.enumcode.EnumMajorType;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "student_major")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StudentMajor extends CreatedUpdatedAt {

    @Id
    @Tsid
    @Column(name = "student_major_id", nullable = false)
    private Long studentMajorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_code", nullable = false)
    private Student student;

    @Column(name = "major_id", nullable = false)
    private Long majorId; //major cache 테이블 참조

    @Column(name = "type", nullable = false, length = 20)
    private EnumMajorType type;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public void deactivate() {
        this.isActive = false;
    }
}