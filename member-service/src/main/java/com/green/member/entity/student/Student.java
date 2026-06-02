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

    @Column(name = "status", nullable = false, length = 20)
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

    public void updateByAdmin(Boolean isTransfer, Boolean isMultiChild, Boolean isVeteran){
        if(isTransfer != null) this.isTransfer = isTransfer;
        if(isMultiChild != null) this.isMultiChild = isMultiChild;
        if(isVeteran != null) this.isVeteran = isVeteran;
    }

    public void updateStatus(EnumStudentStatus status){
        if(status != null) this.status = status;
    }

    // 학기 자동 갱신: 1학기 → 2학기, 2학기 → 다음 학년 1학기 (초과학기 상한 없음)
    public void advanceSemester() {
        if (this.semester == null || this.academicYear == null) return;
        if (this.semester == 1) {
            this.semester = 2;
        } else {
            this.semester = 1;
            this.academicYear = this.academicYear + 1;
        }
    }
}