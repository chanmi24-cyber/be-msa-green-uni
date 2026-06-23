package com.green.common.model;

import com.green.common.enumcode.EnumMemberRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class JwtMember {
    private Long loginMemberCode;
    private String loginMemberRole;
    private String deviceId;
}