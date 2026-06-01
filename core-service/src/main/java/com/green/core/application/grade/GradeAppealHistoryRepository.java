package com.green.core.application.grade;

import com.green.core.entity.grade.GradesAppealHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeAppealHistoryRepository extends JpaRepository<GradesAppealHistory, Long> {
}
