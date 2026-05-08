package com.green.core.repository;

import com.green.core.entity.cache.ScheduleCache;
import com.green.common.enumcode.EnumScheduleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScheduleCacheRepository extends JpaRepository<ScheduleCache, Long> {
    Optional<ScheduleCache> findByTypeAndIsActiveTrue(EnumScheduleType type);
}