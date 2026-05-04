package com.green.member.entity.student;

import com.green.common.entity.UpdatedAt;
import com.green.common.enumcode.EnumStudentStatus;
import com.green.member.entity.member.Member;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "student")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Student extends UpdatedAt {

    @Id
    @Column(name = "member_code")
    private Long memberCode;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_code", nullable = false)
    private Member member;

    @Column(name = "academic_year")
    private Integer academicYear;

    @Column(name = "semester")
    private Integer semester;

    @Column(name = "status", nullable = false, length = 10)
    @Builder.Default
    private EnumStudentStatus status = EnumStudentStatus.UNREGISTERED;

    @Column(name = "is_transfer", nullable = false)
    @Builder.Default
    private Boolean isTransfer = false;

    @Column(name = "is_multi_child", nullable = false)
    @Builder.Default
    private Boolean isMultiChild = false;

    @Column(name = "is_veteran", nullable = false)
    @Builder.Default
    private Boolean isVeteran = false;
}