package com.green.auth.application.auth.model;

import com.green.common.enumcode.EnumMemberRole;
import lombok.Data;

@Data
public class AuthMemberCreateReq {
    private Long memberCode;
    private String password;
    private EnumMemberRole role;
    private String email;
}