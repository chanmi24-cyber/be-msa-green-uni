package com.green.auth.application.auth.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LoginReq {
    @NotBlank
    private Long memberCode;
    @NotBlank
    private String password;
}