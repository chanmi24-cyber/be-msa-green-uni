package com.green.core.application.tuition;

import com.green.core.entity.tuition.TuitionMailLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TuitionMailLogRepository extends JpaRepository<TuitionMailLog, Long> {
}