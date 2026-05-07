package com.green.core.repository;

import com.green.core.entity.cache.ProfessorCache;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorCacheRepository  extends JpaRepository<ProfessorCache, Long> {
}
