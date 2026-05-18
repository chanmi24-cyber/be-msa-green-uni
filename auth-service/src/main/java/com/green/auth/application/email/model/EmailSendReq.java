package com.green.auth.application.email.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class EmailSendReq {
    @NotNull
    private Long memberCode;
    @NotBlank @Email
    private String email;
}