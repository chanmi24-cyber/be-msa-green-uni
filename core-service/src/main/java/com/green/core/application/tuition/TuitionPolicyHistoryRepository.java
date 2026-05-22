package com.green.core.application.tuition;

import com.green.core.entity.tuition.TuitionPolicyHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TuitionPolicyHistoryRepository extends JpaRepository<TuitionPolicyHistory, Long> {
    // 특정 기간(학기 시작일~종료일) 내의 이력을 조회하는 쿼리
    @Query("SELECT h FROM TuitionPolicyHistory h WHERE h.createdAt BETWEEN :start AND :end")
    List<TuitionPolicyHistory> findByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}