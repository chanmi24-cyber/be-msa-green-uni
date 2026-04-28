package com.green.auth.application.model;

import com.green.common.enumcode.EnumMemberRole;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class LoginRes {
    private Integer memberCode;
    private String name;
    private EnumMemberRole role;
    private Boolean isFirstLogin;
}