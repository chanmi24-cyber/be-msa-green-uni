package com.green.core.repository;

import com.green.core.entity.cache.StudentMajorCache;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentMajorCacheRepository extends JpaRepository<StudentMajorCache, Long> {
}
