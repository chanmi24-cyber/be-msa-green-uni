package com.green.core.application.attendance;

import com.green.core.entity.course.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendEnrollmentRepository extends JpaRepository<Course, Long> {

    // 특정 강의의 수강생 전체 조회
    List<Course> findByLecture_LectureId(Long lectureId);

    Optional<Course> findByLecture_LectureIdAndStudentCode(Long lectureId, Long studentCode);
}