package com.green.academic.websocket;

import java.security.Principal;

public record MemberPrincipal(Long memberCode, String role) implements Principal {
    @Override
    public String getName() {
        return memberCode.toString();
    }
}
