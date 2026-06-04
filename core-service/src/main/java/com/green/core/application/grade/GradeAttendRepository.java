package com.green.core.application.grade;

import com.green.core.entity.attendance.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// [추가] 출석 점수 자동 계산용 — attendance 테이블 읽기 전용
public interface GradeAttendRepository extends JpaRepository<Attendance, Long> {

    // 수강(course) 기준 출석 이력 전체 조회 (ABSENT/LATE/EARLY_LEAVE 집계용)
    List<Attendance> findByCourse_CourseId(Long courseId);
}
