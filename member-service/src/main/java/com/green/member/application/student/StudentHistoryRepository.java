package com.green.member.application.student;

import com.green.member.entity.student.StudentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentHistoryRepository extends JpaRepository<StudentHistory, Long> {
    List<StudentHistory> findByStudent_MemberCodeOrderByCreatedAtDesc(Long memberCode);
}
