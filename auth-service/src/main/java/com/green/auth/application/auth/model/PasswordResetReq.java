package com.green.auth.application.auth.model;

import lombok.Data;

@Data
public class PasswordResetReq {
    private String email;
    private String newPassword;
}
