package com.green.core.entity.cache;

import com.green.common.enumcode.EnumMajorType;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "student_major_cache")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StudentMajorCache {

    @Id
    @Column(name = "student_major_id")
    private Long studentMajorId;

    @Column(name = "member_code", nullable = false)
    private Long memberCode;

    @Column(name = "major_id", nullable = false)
    private Long majorId;

    @Column(name = "type", nullable = false, length = 20)
    private EnumMajorType type;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}