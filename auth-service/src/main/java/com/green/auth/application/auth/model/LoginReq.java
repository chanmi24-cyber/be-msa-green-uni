package com.green.auth.application.auth.model;

import lombok.Getter;

@Getter
public class LoginReq {
    private Long memberCode;
    private String password;
}