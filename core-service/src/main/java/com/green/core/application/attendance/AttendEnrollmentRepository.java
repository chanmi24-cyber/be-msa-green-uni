package com.green.core.application.attendance;

import com.green.core.entity.course.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendEnrollmentRepository extends JpaRepository<Course, Long> {

    // 특정 강의의 수강생 전체 조회
    List<Course> findByLecture_LectureId(Long lectureId);

    Optional<Course> findByLecture_LectureIdAndStudentCode(Long lectureId, Long studentCode);

    // [추가] 학생이 수강 중인 강의 ID 목록 — getMyAttendance 세션 기반 조회용
    @org.springframework.data.jpa.repository.Query(
            "SELECT c.lecture.lectureId FROM Course c WHERE c.studentCode = :studentCode")
    List<Long> findLectureIdsByStudentCode(
            @org.springframework.data.repository.query.Param("studentCode") Long studentCode);
}