package com.green.core.application.grade;

import com.green.core.entity.lecture.LectureEvaluation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

// [추가] 강의평가 완료 여부 확인용 — lecture_evaluation 테이블 읽기 전용
public interface GradeEvalRepository extends JpaRepository<LectureEvaluation, Long> {

    // createdAt IS NOT NULL = 평가 제출 완료 (null = 미완료)
    boolean existsByCourse_CourseIdAndCreatedAtIsNotNull(Long courseId);

    // 성적 목록 조회용: 완료된 courseId Set 일괄 조회 (N+1 방지)
    @Query("SELECT e.course.courseId FROM LectureEvaluation e WHERE e.course.courseId IN :courseIds AND e.createdAt IS NOT NULL")
    Set<Long> findCompletedCourseIds(@Param("courseIds") List<Long> courseIds);

    // 성적 상세조회 gate용: 학생의 미완료 평가가 하나라도 있으면 true
    boolean existsByCourse_StudentCodeAndCreatedAtIsNull(Long studentCode);
}