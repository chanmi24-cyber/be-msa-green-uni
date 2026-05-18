package com.green.member.application.professor.model;

import com.green.common.enumcode.EnumBuilding;
import com.green.common.enumcode.EnumProfessorDegree;
import com.green.common.enumcode.EnumProfessorStatus;
import com.green.member.application.member.model.MemberCreateReq;
import com.green.member.enumcode.EnumProfessorPosition;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProfessorCreateReq extends MemberCreateReq {

    @NotNull(message = "전공 ID는 필수입니다.")
    private Long majorId;

    @NotNull(message = "최종 학위는 필수입니다.")
    private EnumProfessorDegree degree;

    @NotNull(message = "현재 직위 필수입니다.")
    private EnumProfessorPosition position;
    private EnumBuilding labBuilding;
    private String labRoom;
    private String labTel;

    @NotNull(message = "현재 상태는 필수입니다.")
    private EnumProfessorStatus status;
}
