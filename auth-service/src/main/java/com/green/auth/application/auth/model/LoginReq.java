package com.green.auth.application.auth.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class LoginReq {
    @NotNull
    private Long memberCode;
    @NotNull
    private String password;
}