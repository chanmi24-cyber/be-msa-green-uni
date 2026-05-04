package com.green.core.repository;

import com.green.core.entity.cache.StudentCache;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentCacheRepository extends JpaRepository<StudentCache, Long> {
}
