package com.green.common.security;

import com.green.common.model.EnumMemberRole;
import com.green.common.model.JwtMember;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {
    private final JwtMember jwtMember;

    // 로그인 유저 아이디 반환
    public long getLoginMemberId() { return jwtMember.getLoginMemberCode(); }

    // 로그인 유저 구분/권한 반환
    public EnumMemberRole getLoginMemberRole() { return jwtMember.getLoginMemberRole(); }

    @Override // Spring Security가 유저의 권한을 물어볼때 호출하는 메소드. Collection: 한 유저가 여러개 권한 가질수 있음
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleName = String.format("ROLE_%s", jwtMember.getLoginMemberRole().name());
        return List.of(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public @Nullable String getPassword() { return ""; }

    @Override
    public String getUsername() { return ""; }
}
