package com.green.common.kafka.auth;

import com.green.common.constants.EventType;
import com.green.common.enumcode.EnumMemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthMemberEvent implements Serializable {
    private Long memberCode;
    private String email;
    private String password;
    private String role;
    private EventType eventType;
}