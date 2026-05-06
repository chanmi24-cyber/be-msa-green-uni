package com.green.member.application.professor;

import com.green.member.entity.professor.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository extends JpaRepository<Professor,Long> {
}
