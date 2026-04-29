package com.green.auth.application.auth.model;

import lombok.Getter;

@Getter
public class LoginReq {
    private Integer memberCode;
    private String password;
}