package com.green.auth.application.model;

import com.green.common.model.EnumMemberRole;
import lombok.Data;

@Data
public class MemberCreateReq {
    private Integer memberCode;
    private String password;
//    private EnumMemberRole role;
    private String email;
}