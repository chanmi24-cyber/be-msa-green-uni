package com.green.auth.application.email.model;

import lombok.Getter;

@Getter
public class EmailVerifyReq {
    private String email;
    private String verifyCode;
}