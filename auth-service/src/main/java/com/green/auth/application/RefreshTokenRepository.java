package com.green.auth.application;

import com.green.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    RefreshToken findByAuthMember_MemberCode(Integer memberCode); //회원코드로 해당 회원의 RT 조회시 사용. 재발급시 필요
    void deleteByAuthMember_MemberCode(Integer memberCode); //로그아웃시 RT 삭제
}
