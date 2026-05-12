package com.green.auth.application.auth.model;

import com.green.common.enumcode.EnumMemberRole;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class LoginRes {
    private Long memberCode;
    private String role;
    private String deviceId;
    private Boolean isFirstLogin;
}