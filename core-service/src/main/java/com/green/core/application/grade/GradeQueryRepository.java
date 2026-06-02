package com.green.core.application.grade;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class GradeQueryRepository {

    @PersistenceContext
    private EntityManager em;

    // 학생의 F학점 제외 총 취득학점 합산 (학적 변경신청, 졸업 처리에 사용)
    public int sumTotalCreditsByStudentCode(Long studentCode) {
        Number result = (Number) em.createNativeQuery("""
                SELECT COALESCE(SUM(l.credit), 0)
                FROM grade g
                INNER JOIN course c ON c.course_id = g.course_id
                INNER JOIN lecture l ON l.lecture_id = c.lecture_id
                WHERE c.student_code = :studentCode
                  AND g.grade_letter IS NOT NULL
                  AND g.grade_letter != 'F'
                """)
                .setParameter("studentCode", studentCode)
                .getSingleResult();
        return result.intValue();
    }

    // 학생의 weighted GPA 계산 (F 포함 전체 성적 기준) (전공변경 신청에 사용)
    public Double calcWeightedGpaByStudentCode(Long studentCode) {
        Number result = (Number) em.createNativeQuery("""
                SELECT COALESCE(
                    SUM(
                        CASE g.grade_letter
                            WHEN 'A+' THEN 4.5
                            WHEN 'A'  THEN 4.0
                            WHEN 'B+' THEN 3.5
                            WHEN 'B'  THEN 3.0
                            WHEN 'C+' THEN 2.5
                            WHEN 'C'  THEN 2.0
                            WHEN 'D+' THEN 1.5
                            WHEN 'D'  THEN 1.0
                            ELSE 0.0
                        END * l.credit
                    ) / NULLIF(SUM(l.credit), 0)
                , 0.0)
                FROM grade g
                INNER JOIN course c ON c.course_id = g.course_id
                INNER JOIN lecture l ON l.lecture_id = c.lecture_id
                WHERE c.student_code = :studentCode
                  AND g.grade_letter IS NOT NULL
                """)
                .setParameter("studentCode", studentCode)
                .getSingleResult();
        return result != null ? result.doubleValue() : null;
    }
}
