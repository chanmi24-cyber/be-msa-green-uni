package com.green.auth.application.email.model;

import lombok.Getter;

@Getter
public class EmailSendReq {
    private Integer memberCode;
    private String email;
}