package com.green.auth.application.email.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class EmailSendReq {
    private Long memberCode;
    @Email
    private String email;
}