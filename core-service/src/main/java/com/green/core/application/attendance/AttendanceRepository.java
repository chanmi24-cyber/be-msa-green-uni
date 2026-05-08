package com.green.core.application.attendance;

import com.green.core.entity.attendance.Attendance;
import com.green.core.entity.attendance.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

// AttendanceRepository.java
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

  // 특정 강의 + 특정 날짜에 출석 기록이 있는 학생 ID 목록
  // "오늘 이미 스캔한 학생" 판별에 사용
  @Query("SELECT a.studentCode FROM Attendance a WHERE a.course.lecture.lectureId = :lectureId " +
          "AND a.attendsession.classDate = :classDate")
  List<Long> findStudentCodeByLectureIdAndClassDate(
        @Param("lectureId") Long lectureId,
        @Param("classDate") LocalDate classDate);

  boolean existsByAttendsessionAndStudentCode(AttendanceSession attendsession, Long studentCode);
}