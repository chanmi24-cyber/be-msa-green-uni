package com.green.auth.application.model.email;

import lombok.Getter;

@Getter
public class EmailVerifyReq {
    private String email;
    private String verifyCode;
}