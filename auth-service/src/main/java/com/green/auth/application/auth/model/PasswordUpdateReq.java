package com.green.auth.application.auth.model;

import lombok.Data;

@Data
public class PasswordUpdateReq {
    private String oldPassword;
    private String newPassword;
}
