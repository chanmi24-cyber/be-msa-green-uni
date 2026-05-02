package com.green.auth.application.email.model;

import lombok.Getter;

@Getter
public class EmailSendReq {
    private Long memberCode;
    private String email;
}