package com.green.member.application.professor;

import com.green.member.entity.professor.ProfessorHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfessorHistoryRepository extends JpaRepository<ProfessorHistory, Long> {
    List<ProfessorHistory> findByProfessor_MemberCodeOrderByCreatedAtDesc(Long memberCode);
}
