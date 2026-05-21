package com.green.member.application.schedule;

import com.green.common.enumcode.EnumScheduleType;
import com.green.member.entity.cache.ScheduleCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScheduleCacheRepository extends JpaRepository<ScheduleCache, Long> {
    Optional<ScheduleCache> findByTypeAndIsActiveTrue(EnumScheduleType type);
}