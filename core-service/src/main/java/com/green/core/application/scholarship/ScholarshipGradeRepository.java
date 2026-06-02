package com.green.core.application.scholarship;

import com.green.core.entity.grade.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

//충돌 방지를 위해 성적 장학금 로직을 위한 레파지토리는 새로 만듦
public interface ScholarshipGradeRepository extends JpaRepository<Grade, Long> {

    // 특정 년도/학기의 학생별 평균 점수 조회 (학과 + 학년 그룹핑용)
    // isDel = false 인 Course만 대상
    @Query("""
    SELECT
        c.studentCode,
        l.major.majorId,
        CASE WHEN l.academicYear >= 4 THEN 4 ELSE l.academicYear END,
        AVG(g.totalScore)
    FROM Grade g
    JOIN g.course c
    JOIN c.lecture l
    WHERE c.year = :year
      AND c.semester = :semester
      AND c.isDel = false
      AND l.status = com.green.common.enumcode.EnumApprovalStatus.APPROVED
    GROUP BY c.studentCode, l.major.majorId,
             CASE WHEN l.academicYear >= 4 THEN 4 ELSE l.academicYear END
    HAVING AVG(g.totalScore) >= 90
""")
    List<Object[]> findAvgScoreGroupedByMajorAndAcademicYear(
            @Param("year") Integer year,
            @Param("semester") Integer semester
    );
}