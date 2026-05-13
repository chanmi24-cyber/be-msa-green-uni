package com.green.member.application.student;

import com.green.member.entity.student.StudentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentHistoryRepository extends JpaRepository<StudentHistory, Long> {
}
