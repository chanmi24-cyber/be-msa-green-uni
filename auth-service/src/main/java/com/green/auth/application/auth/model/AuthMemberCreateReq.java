package com.green.auth.application.auth.model;

import com.green.common.enumcode.EnumMemberRole;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AuthMemberCreateReq {
    private Long memberCode;
    private String password;
    private String email;
    private EnumMemberRole role;
}