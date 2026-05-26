package com.green.academic.application.schedule;

import com.green.academic.entity.Schedule;
import com.green.common.enumcode.EnumScheduleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long>, JpaSpecificationExecutor<Schedule> {
    List<Schedule> findByIsActiveTrue();

    @Modifying
    @Transactional
    @Query("UPDATE Schedule s SET s.isNotifiedStart = true WHERE s.scheduleId = :id AND s.isNotifiedStart = false")
    int markNotifiedStartIfFalse(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Schedule s SET s.isNotifiedThreeDaysBefore = true WHERE s.scheduleId = :id AND s.isNotifiedThreeDaysBefore = false")
    int markNotifiedThreeDaysBeforeIfFalse(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Schedule s SET s.isNotifiedEnd = true WHERE s.scheduleId = :id AND s.isNotifiedEnd = false")
    int markNotifiedEndIfFalse(@Param("id") Long id);
}