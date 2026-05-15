package com.green.auth.application.auth.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PasswordUpdateReq {
    @NotBlank
    private String oldPassword;
    @NotBlank
    private String newPassword;
}
