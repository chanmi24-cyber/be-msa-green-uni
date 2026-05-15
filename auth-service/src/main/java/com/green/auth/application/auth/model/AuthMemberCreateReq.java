package com.green.auth.application.auth.model;

import com.green.common.enumcode.EnumMemberRole;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Builder
@Getter
public class AuthMemberCreateReq {
    private Long memberCode;
    private String password;
    private String email;
    private EnumMemberRole role;
}