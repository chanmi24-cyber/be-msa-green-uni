package com.green.member.entity;

import com.green.common.entity.UpdatedAt;
import com.green.member.enumcode.EnumProfessorDegree;
import com.green.member.enumcode.EnumProfessorPosition;
import com.green.common.enumcode.EnumProfessorStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "professor")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Professor extends UpdatedAt {

    @Id
    @Column(name = "member_code")
    private Long memberCode;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_code", nullable = false)
    private Member member;

    @Column(name = "major_id", nullable = false)
    private Long majorId; // major cache테이블 참조

    @Column(name = "degree", nullable = false, length = 20)
    private EnumProfessorDegree degree;

    @Column(name = "position", nullable = false, length = 20)
    @Builder.Default
    private EnumProfessorPosition position = EnumProfessorPosition.PROFESSOR;

    @Column(name = "lab_room", length = 20)
    private String labRoom;

    @Column(name = "lab_tel", length = 20)
    private String labTel;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EnumProfessorStatus status = EnumProfessorStatus.EMPLOYMENT;
}