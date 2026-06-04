package com.green.member.application.student;

import com.green.common.enumcode.EnumStudentStatus;
import com.green.member.application.student.model.StudentListRes;
import com.green.member.entity.student.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    // 원본 테이블 상태 변경을 위한 벌크 쿼리 메서드 추가
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Student s SET s.status = :status WHERE s.memberCode = :memberCode")
    int updateStatus(@Param("memberCode") Long memberCode,
                     @Param("status") EnumStudentStatus status);

    // 학기 갱신 / 졸업 처리 대상 조회
    List<Student> findByStatus(EnumStudentStatus status);

    // 관리자 학생 목록 조회 (status/collegeName/majorName/academicYear/search 필터 + 페이지네이션)
    @Query(
            value = """
            SELECT m.member_code     AS memberCode,
                   m.name            AS name,
                   m.email           AS email,
                   m.tel             AS tel,
                   s.status          AS status,
                   s.academic_year   AS academicYear,
                   s.semester        AS semester,
                   mc_p.college_name AS college,
                   mc_p.name         AS majorName,
                   mc_d.name         AS minorName
            FROM student s
            JOIN member m
              ON m.member_code = s.member_code
            JOIN student_major sm_p
              ON sm_p.student_code = s.member_code
             AND sm_p.type = 'PRIMARY'
             AND sm_p.is_active = true
            JOIN major_cache mc_p
              ON mc_p.major_id = sm_p.major_id
            LEFT JOIN student_major sm_d
              ON sm_d.student_code = s.member_code
             AND sm_d.type = 'MINOR'
             AND sm_d.is_active = true
            LEFT JOIN major_cache mc_d
              ON mc_d.major_id = sm_d.major_id
            WHERE (:status IS NULL OR s.status = :status)
              AND (:academicYear IS NULL OR s.academic_year = :academicYear)
              AND (:collegeName IS NULL OR mc_p.college_name = :collegeName)
              AND (:majorName IS NULL OR mc_p.name = :majorName)
              AND (:search IS NULL OR m.name LIKE CONCAT('%', :search, '%'))
            ORDER BY m.member_code DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM student s
            JOIN member m
              ON m.member_code = s.member_code
            JOIN student_major sm_p
              ON sm_p.student_code = s.member_code
             AND sm_p.type = 'PRIMARY'
             AND sm_p.is_active = true
            JOIN major_cache mc_p
              ON mc_p.major_id = sm_p.major_id
            WHERE (:status IS NULL OR s.status = :status)
              AND (:academicYear IS NULL OR s.academic_year = :academicYear)
              AND (:collegeName IS NULL OR mc_p.college_name = :collegeName)
              AND (:majorName IS NULL OR mc_p.name = :majorName)
              AND (:search IS NULL OR m.name LIKE CONCAT('%', :search, '%'))
            """,
            nativeQuery = true
    )
    Page<StudentListRes> findStudentList(
            @Param("status") String status,
            @Param("academicYear") Integer academicYear,
            @Param("collegeName") String collegeName,
            @Param("majorName") String majorName,
            @Param("search") String search,
            Pageable pageable
    );
}