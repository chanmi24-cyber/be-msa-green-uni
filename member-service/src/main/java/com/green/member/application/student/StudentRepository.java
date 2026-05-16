package com.green.member.application.student;

import com.green.member.application.admin.model.StudentListDto;
import com.green.member.entity.student.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query(
        value = """
            SELECT m.member_code   AS memberCode,
                   m.name          AS name,
                   m.email         AS email,
                   m.tel           AS tel,
                   s.status        AS status,
                   s.academic_year AS academicYear,
                   s.semester      AS semester,
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
              AND (:collegeId IS NULL OR mc_p.college_id = :collegeId)
              AND (:academicYear IS NULL OR s.academic_year = :academicYear)
              AND (:majorName IS NULL OR mc_p.name LIKE CONCAT('%', :majorName, '%'))
              AND (:name IS NULL OR m.name LIKE CONCAT('%', :name, '%'))
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
            LEFT JOIN student_major sm_d
              ON sm_d.student_code = s.member_code
             AND sm_d.type = 'MINOR'
             AND sm_d.is_active = true
            LEFT JOIN major_cache mc_d
              ON mc_d.major_id = sm_d.major_id
            WHERE (:status IS NULL OR s.status = :status)
              AND (:collegeId IS NULL OR mc_p.college_id = :collegeId)
              AND (:academicYear IS NULL OR s.academic_year = :academicYear)
              AND (:majorName IS NULL OR mc_p.name LIKE CONCAT('%', :majorName, '%'))
              AND (:name IS NULL OR m.name LIKE CONCAT('%', :name, '%'))
            """,
        nativeQuery = true
    )
    Page<StudentListDto> findStudentList(
        @Param("status") String status,
        @Param("collegeId") Long collegeId,
        @Param("academicYear") Integer academicYear,
        @Param("majorName") String majorName,
        @Param("name") String name,
        Pageable pageable
    );
}
