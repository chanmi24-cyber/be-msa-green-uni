package com.green.member.application.student;

import com.green.common.enumcode.EnumStudentStatus;
import com.green.member.application.student.model.StudentListRes;
import com.green.member.entity.student.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    // 학기 갱신 / 졸업 처리 대상 조회
    List<Student> findByStatus(EnumStudentStatus status);

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
            ORDER BY m.member_code DESC
            """,
            nativeQuery = true
    )
    List<StudentListRes> findStudentList();
}
