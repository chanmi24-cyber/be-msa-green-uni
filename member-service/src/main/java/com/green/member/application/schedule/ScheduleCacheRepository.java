package com.green.member.application.schedule;

import com.green.common.enumcode.EnumScheduleType;
import com.green.member.entity.cache.ScheduleCache;
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
    // 오늘이 시작일인 활성화된 스케줄 조회 (학기 시작/종료 트리거 판단용)
    List<ScheduleCache> findByTypeAndIsActiveTrueAndStartDateBetween(
            EnumScheduleType type, LocalDateTime start, LocalDateTime end);
}