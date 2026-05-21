package com.green.member.application.student;

import com.green.common.enumcode.EnumMajorType;
import com.green.member.entity.student.StudentMajor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentMajorRepository extends JpaRepository<StudentMajor, Long> {
    List<StudentMajor> findByStudent_MemberCodeAndIsActiveTrue(Long memberCode);
    Optional<StudentMajor> findByStudent_MemberCodeAndTypeAndIsActiveTrue(Long memberCode, EnumMajorType type);
}
