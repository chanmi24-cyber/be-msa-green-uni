package com.green.core.application.grade;

import com.green.core.entity.grade.Grade;
import com.green.core.enumcode.EnumGradeLetter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// [추가] repository/GradeRepository 에서 이동 + 성적 도메인 전용 쿼리 추가
public interface GradeRepository extends JpaRepository<Grade, Long> {

    // 교수 성적 조회: 강의의 수강생 전체 성적
    @Query("SELECT g FROM Grade g " +
           "JOIN FETCH g.course c " +
           "JOIN FETCH c.lecture l " +
           "WHERE l.lectureId = :lectureId " +
           "ORDER BY c.studentCode")
    List<Grade> findByLectureId(@Param("lectureId") Long lectureId);

    // 학생 성적 목록 — 전체
    @Query("SELECT g FROM Grade g " +
           "JOIN FETCH g.course c " +
           "JOIN FETCH c.lecture l " +
           "WHERE c.studentCode = :studentCode " +
           "ORDER BY c.year DESC, c.semester DESC, l.lectureName")
    List<Grade> findByStudentCode(@Param("studentCode") Long studentCode);

    // 학생 성적 목록 — 연도+학기 필터
    @Query("SELECT g FROM Grade g " +
           "JOIN FETCH g.course c " +
           "JOIN FETCH c.lecture l " +
           "WHERE c.studentCode = :studentCode AND c.year = :year AND c.semester = :semester " +
           "ORDER BY l.lectureName")
    List<Grade> findByStudentCodeAndYearAndSemester(
            @Param("studentCode") Long studentCode,
            @Param("year") Integer year,
            @Param("semester") Integer semester);

    // 학생 성적 목록 — 연도만 필터
    @Query("SELECT g FROM Grade g " +
           "JOIN FETCH g.course c " +
           "JOIN FETCH c.lecture l " +
           "WHERE c.studentCode = :studentCode AND c.year = :year " +
           "ORDER BY c.semester DESC, l.lectureName")
    List<Grade> findByStudentCodeAndYear(
            @Param("studentCode") Long studentCode,
            @Param("year") Integer year);

    // 학생 성적 상세 (courseId = gradeId, Major까지 JOIN)
    @Query("SELECT g FROM Grade g " +
           "JOIN FETCH g.course c " +
           "JOIN FETCH c.lecture l " +
           "JOIN FETCH l.major m " +
           "WHERE g.courseId = :gradeId")
    Optional<Grade> findDetailById(@Param("gradeId") Long gradeId);

    // 석차: 같은 강의에서 나보다 totalScore가 높은 사람 수
    @Query("SELECT COUNT(g) FROM Grade g " +
           "WHERE g.course.lecture.lectureId = :lectureId AND g.totalScore > :totalScore")
    int countHigherScore(@Param("lectureId") Long lectureId, @Param("totalScore") int totalScore);

    // 전체 수강생 수 (석차 분모)
    @Query("SELECT COUNT(g) FROM Grade g WHERE g.course.lecture.lectureId = :lectureId")
    int countByLectureId(@Param("lectureId") Long lectureId);

    // 학과 석차: 같은 학과 재학생 중 나보다 weighted GPA 높은 사람 수
    @Query(value = """
            SELECT COUNT(DISTINCT sq.student_code)
            FROM (
                SELECT c.student_code,
                       SUM(g.total_score * l.credit) / NULLIF(SUM(l.credit), 0) AS avg_gpa
                FROM grade g
                INNER JOIN course c ON c.course_id = g.course_id
                INNER JOIN lecture l ON l.lecture_id = c.lecture_id
                INNER JOIN student_cache sc ON sc.member_code = c.student_code
                WHERE sc.major_id = :majorId
                  AND sc.status = 'ENROLLED'
                  AND g.grade_letter IS NOT NULL
                GROUP BY c.student_code
            ) sq
            WHERE sq.avg_gpa > :myGpa
            """, nativeQuery = true)
    int countMajorStudentsWithHigherGpa(@Param("majorId") Long majorId, @Param("myGpa") double myGpa);

    // 학과 석차 분모: 같은 학과 재학생 중 성적이 입력된 학생 수
    @Query(value = """
            SELECT COUNT(DISTINCT c.student_code)
            FROM grade g
            INNER JOIN course c ON c.course_id = g.course_id
            INNER JOIN student_cache sc ON sc.member_code = c.student_code
            WHERE sc.major_id = :majorId
              AND sc.status = 'ENROLLED'
              AND g.grade_letter IS NOT NULL
            """, nativeQuery = true)
    int countMajorStudentsWithGrades(@Param("majorId") Long majorId);

    // 성적 점수 일괄 업데이트
    @Modifying
    @Query("UPDATE Grade g SET " +
           "g.midScore = :midScore, " +
           "g.finScore = :finScore, " +
           "g.assignmentScore = :assignmentScore, " +
           "g.attendScore = :attendScore, " +
           "g.totalScore = :totalScore, " +
           "g.gradeLetter = :gradeLetter " +
           "WHERE g.courseId = :courseId")
    void updateScores(
            @Param("courseId") Long courseId,
            @Param("midScore") int midScore,
            @Param("finScore") int finScore,
            @Param("assignmentScore") int assignmentScore,
            @Param("attendScore") int attendScore,
            @Param("totalScore") int totalScore,
            @Param("gradeLetter") EnumGradeLetter gradeLetter);

}
