package com.green.core.repository;

import com.green.core.entity.cache.ScheduleCache;
import com.green.common.enumcode.EnumScheduleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleCacheRepository extends JpaRepository<ScheduleCache, Long> {
    List<ScheduleCache> findByTypeAndIsActiveTrue(EnumScheduleType type);

    boolean existsByTypeAndYearAndSemester(
            EnumScheduleType type, Integer year, Integer semester
    );
}