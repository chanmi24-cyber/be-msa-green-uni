package com.green.core.entity.cache;

import com.green.common.enumcode.EnumStudentStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_cache")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StudentCache {

    @Id
    @Column(name = "member_code")
    private Long memberCode;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "email", nullable = false, length = 50)
    private String email;

    @Column(name = "major_id", nullable = false)
    private Long majorId;

    @Column(name = "minor_id")
    private Long minorId; // 부전공 없으면 null

    @Column(name = "academic_year")
    private Integer academicYear;

    @Column(name = "semester")
    private Integer semester;

    @Column(name = "status", nullable = false, length = 20)
    private EnumStudentStatus status;

    @Column(name = "is_transfer", nullable = false)
    private Boolean isTransfer;

    @Column(name = "is_multi_child", nullable = false)
    private Boolean isMultiChild;

    @Column(name = "is_veteran", nullable = false)
    private Boolean isVeteran;

    // 🎯 [핵심 추가] 비즈니스 로직에 따른 상태 변경 전용 메서드
    public void updateStatus(EnumStudentStatus status) {
        this.status = status;
    }
}