package com.green.common.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

//Spring Security가 인증처리를 할 때 사용하는 객체
@Getter
@RequiredArgsConstructor
@ToString
public class UserPrincipal implements UserDetails {
    private final JwtMember jwtMember;

    public long getLoginMemberCode() {
        return jwtMember.getLoginMemberCode();
    }

    //인가 체크
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleName = String.format("ROLE_%s", jwtMember.getLoginMemberRole().name());
        return List.of(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public @Nullable String getPassword() {
        return "";
    }
    @Override
    public String getUsername() {
        return String.valueOf(jwtMember.getLoginMemberCode());
    }
}