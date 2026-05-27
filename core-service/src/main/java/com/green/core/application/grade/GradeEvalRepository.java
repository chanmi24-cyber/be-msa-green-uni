package com.green.core.application.grade;

import com.green.core.entity.lecture.LectureEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

// [추가] 강의평가 완료 여부 확인용 — lecture_evaluation 테이블 읽기 전용
public interface GradeEvalRepository extends JpaRepository<LectureEvaluation, Long> {

    // createdAt IS NOT NULL = 평가 제출 완료 (null = 미완료)
    boolean existsByCourse_CourseIdAndCreatedAtIsNotNull(Long courseId);
}