package com.green.core.application.major;

import com.green.core.entity.cache.ProfessorCache;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorCacheRepository extends JpaRepository<ProfessorCache, Long> {
}