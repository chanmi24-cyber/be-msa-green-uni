package com.green.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtMember {
    private long loginUserId;
    private EnumMemberRole enumMemberRole;
}
