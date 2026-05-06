package com.green.member.application.student;

import com.green.member.entity.student.StudentMajor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentMajorRepository extends JpaRepository<StudentMajor, Long> {
}
