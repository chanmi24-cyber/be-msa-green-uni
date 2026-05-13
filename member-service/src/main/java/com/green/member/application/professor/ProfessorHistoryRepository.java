package com.green.member.application.professor;

import com.green.member.entity.professor.ProfessorHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorHistoryRepository extends JpaRepository<ProfessorHistory, Long> {
}
