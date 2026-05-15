package com.green.auth.application.auth.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class LoginReq {
    @NotNull
    private Long memberCode;
    @NotBlank
    private String password;
}