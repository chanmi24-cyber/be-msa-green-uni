package com.green.member.application.member.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class MemberCreateReq {
    private String email;
    private String name;
    private LocalDate birth;
    private String tel;
    private String emergencyTel;
    private String postcode;
    private String address;
    private String detailAddress;
    private LocalDate entryDate;
    private LocalDate exitDate;
    private String pic;
}
