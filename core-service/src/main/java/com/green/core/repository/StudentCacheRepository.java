package com.green.core.repository;

import com.green.core.entity.cache.StudentCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentCacheRepository extends JpaRepository<StudentCache, Long> {
    @Modifying
    @Query("UPDATE StudentCache s SET s.email = :email WHERE s.memberCode = :memberCode")
    void updateEmail(@Param("memberCode") Long memberCode, @Param("email") String email);
}
