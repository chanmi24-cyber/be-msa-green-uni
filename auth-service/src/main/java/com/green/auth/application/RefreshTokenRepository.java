package com.green.auth.application;

import com.green.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    void deleteByAuthMember_MemberCode(Integer memberCode); //로그아웃시 RT 삭제
    RefreshToken findByTokenValue(String tokenValue); // 토큰 재발급시 필요
}