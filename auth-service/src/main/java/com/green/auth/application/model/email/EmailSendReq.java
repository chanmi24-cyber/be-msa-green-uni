package com.green.auth.application.model.email;

import lombok.Getter;

@Getter
public class EmailSendReq {
    private Integer memberCode;
    private String email;
}