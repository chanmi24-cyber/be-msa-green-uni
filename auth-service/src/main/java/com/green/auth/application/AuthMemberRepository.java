package com.green.auth.application;

import com.green.auth.entity.AuthMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthMemberRepository extends JpaRepository<AuthMember, Integer> {

}