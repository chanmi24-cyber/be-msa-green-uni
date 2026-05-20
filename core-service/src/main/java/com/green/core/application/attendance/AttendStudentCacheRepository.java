package com.green.core.application.attendance;

import com.green.core.entity.cache.StudentCache;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendStudentCacheRepository extends JpaRepository<StudentCache, Long> {
}