package com.green.auth.application.auth.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AuthMemberCreateRes {
    private Long memberCode;
}
