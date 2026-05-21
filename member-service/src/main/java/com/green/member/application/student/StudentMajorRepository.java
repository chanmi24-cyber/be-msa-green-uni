package com.green.member.application.student;

import com.green.common.enumcode.EnumMajorType;
import com.green.member.application.admin.model.CurrentMajorDto;
import com.green.member.entity.student.StudentMajor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentMajorRepository extends JpaRepository<StudentMajor, Long> {
    List<StudentMajor> findByStudent_MemberCodeAndIsActiveTrue(Long memberCode);
    Optional<StudentMajor> findByStudent_MemberCodeAndTypeAndIsActiveTrue(Long memberCode, EnumMajorType type);

    // 학생의 현재 활성화된 전공 목록 조회 (전공명 + 전공 구분)
    @Query(value = """
            SELECT mc.name  AS majorName,
                   sm.type  AS majorType
            FROM student_major sm
            JOIN major_cache mc ON mc.major_id = sm.major_id
            WHERE sm.student_code = :memberCode
              AND sm.is_active = true
            """, nativeQuery = true)
    List<CurrentMajorDto> findCurrentMajors(@Param("memberCode") Long memberCode);
}
