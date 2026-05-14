package com.green.auth.application.email.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class EmailVerifyReq {
    @Email
    private String email;
    @NotBlank
    private String verifyCode;
}