package com.green.core.repository;

import com.green.core.entity.cache.ScheduleCache;
import com.green.common.enumcode.EnumScheduleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleCacheRepository extends JpaRepository<ScheduleCache, Long> {
    List<ScheduleCache> findByTypeAndIsActiveTrue(EnumScheduleType type);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM ScheduleCache sc WHERE sc.scheduleId = :scheduleId")
    void deleteByScheduleId(@Param("scheduleId") Long scheduleId);
    List<ScheduleCache> findByTypeAndIsActiveTrueAndStartDateBetween(
            EnumScheduleType type, LocalDateTime start, LocalDateTime end);
    boolean existsByTypeAndYearAndSemester(
            EnumScheduleType type, Integer year, Integer semester
    );
}