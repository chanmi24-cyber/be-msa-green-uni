package com.green.common.model;

import com.green.common.enumcode.EnumMemberRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class JwtMember {
    private Integer loginMemberCode;
    private EnumMemberRole loginMemberRole;
}