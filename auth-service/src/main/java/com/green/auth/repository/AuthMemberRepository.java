package com.green.auth.repository;

import com.green.auth.entity.AuthMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthMemberRepository extends JpaRepository<AuthMember, Integer> {
    boolean existsByMemberCodeAndEmail(Integer memberCode, String email);
}