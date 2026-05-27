package com.green.member.application.schedule;

import com.green.common.enumcode.EnumScheduleType;
import com.green.member.entity.cache.ScheduleCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleCacheRepository extends JpaRepository<ScheduleCache, Long> {
    List<ScheduleCache> findByTypeAndIsActiveTrue(EnumScheduleType type);
    void deleteByScheduleId(Long scheduleId);
}