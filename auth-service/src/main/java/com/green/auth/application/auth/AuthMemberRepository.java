package com.green.auth.application.auth;

import com.green.auth.entity.AuthMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthMemberRepository extends JpaRepository<AuthMember, Long> {
    boolean existsByMemberCodeAndEmail(Long memberCode, String email);
    boolean existsByEmail(String email);
}