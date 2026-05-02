package com.green.auth.application.auth.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AuthMemberDeleteRes {
    private Long memberCode;
}
