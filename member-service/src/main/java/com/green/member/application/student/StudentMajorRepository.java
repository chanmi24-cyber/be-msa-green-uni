package com.green.member.application.student;

import com.green.member.entity.student.StudentMajor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentMajorRepository extends JpaRepository<StudentMajor, Long> {
    List<StudentMajor> findByStudent_MemberCodeAndIsActiveTrue(Long memberCode);
}
