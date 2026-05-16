package com.green.core.application.lecture.repository;

import com.green.core.entity.lecture.LectureEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationRepository extends JpaRepository<LectureEvaluation, Long> {
    // course.studentCode + course.lecture.lectureId 로 중복 체크
    boolean existsByCourse_StudentCodeAndLecture_LectureId(Long studentCode, Long lectureId);
}