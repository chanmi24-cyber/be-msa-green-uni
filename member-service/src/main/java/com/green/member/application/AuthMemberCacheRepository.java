package com.green.member.application;

import com.green.member.entity.AuthMemberCache;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthMemberCacheRepository extends JpaRepository<AuthMemberCache, Integer>{
}
