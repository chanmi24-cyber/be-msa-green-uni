package com.green.auth.application.model;

import lombok.Data;

@Data
public class MemberCreateReq {
    private Integer memberCode;
    private String password;
    private String email;
}
