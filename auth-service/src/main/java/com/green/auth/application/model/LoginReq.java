package com.green.auth.application.model;

import lombok.Getter;

@Getter
public class LoginReq {
    private Integer memberCode;
    private String password;
}