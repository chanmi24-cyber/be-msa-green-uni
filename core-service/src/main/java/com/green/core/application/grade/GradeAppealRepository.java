package com.green.core.application.grade;

import com.green.core.entity.grade.GradesAppeal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeAppealRepository extends JpaRepository<GradesAppeal, Long> {
}