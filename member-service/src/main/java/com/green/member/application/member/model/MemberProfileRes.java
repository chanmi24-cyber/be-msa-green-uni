package com.green.member.application.member.model;

import com.green.common.enumcode.EnumMemberRole;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@SuperBuilder
@ToString
public class MemberProfileRes {
    private Long memberCode;
    private EnumMemberRole role;
    private String name;
    private LocalDate birth;
    private String pic;
    private String email;
    private String tel;
    private String emergencyTel;
    private String postcode;
    private String address;
    private String detailAddress;
    private LocalDate entryDate;
    private LocalDate exitDate;
}
