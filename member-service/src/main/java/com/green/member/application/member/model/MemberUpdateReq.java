package com.green.member.application.member.model;

import com.green.common.enumcode.EnumBuilding;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberUpdateReq {
    // 공통 수정 가능 필드
    private String tel;
    private String emergencyTel;
    private String postcode;
    private String address;
    private String detailAddress;
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    // 교수 전용 (nullable)
    private EnumBuilding labBuilding;
    private String labRoom;
    private String labTel;
}
