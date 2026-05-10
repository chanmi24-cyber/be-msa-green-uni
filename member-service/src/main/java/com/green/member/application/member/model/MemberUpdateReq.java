package com.green.member.application.member.model;

import com.green.common.enumcode.EnumBuilding;
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
    private String pic;

    // 교수 전용 (nullable)
    private EnumBuilding labBuilding;
    private String labRoom;
    private String labTel;
}
