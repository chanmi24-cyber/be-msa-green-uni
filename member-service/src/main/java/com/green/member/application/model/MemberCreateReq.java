package com.green.member.application.model;

import lombok.Data;
import java.time.LocalDate;

@Data
public class MemberCreateReq {
    private String email;
    private String name;
    private String birth;
    private String tel;
    private String emergencyTel;
    private String postcode;
    private String address;
    private String detailAddress;
    private LocalDate entryDate;
    private LocalDate exitDate;
}
