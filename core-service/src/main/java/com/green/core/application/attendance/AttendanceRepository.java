package com.green.core.application.attendance;

import com.green.core.entity.attendance.Attendance;
import com.green.core.entity.attendance.AttendanceSession;
import com.green.core.enumcode.EnumAttendStatus;
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

  // [추가] 보강 스캔 시 CANCEL 세션의 ABSENT 레코드 조회용
  java.util.Optional<Attendance> findByAttendsessionAndStudentCode(AttendanceSession attendsession, Long studentCode);

  List<Attendance> findByAttendsession_AttendsessionId(Long sessionId);

  // N+1 방지: 강의별 출석 상태 GROUP BY 집계 (성적 계산용)
  @Query("SELECT a.course.courseId, COUNT(a) FROM Attendance a " +
         "WHERE a.course.lecture.lectureId = :lectureId " +
         "AND a.status = :status GROUP BY a.course.courseId")
  List<Object[]> countGroupByCourseAndStatus(
          @Param("lectureId") Long lectureId,
          @Param("status") EnumAttendStatus status);

  @Query("SELECT a FROM Attendance a JOIN FETCH a.attendsession s WHERE a.attendId = :attendId AND s.lecture.lectureId = :lectureId")
  java.util.Optional<Attendance> findByAttendIdAndLectureId(@Param("lectureId") Long lectureId, @Param("attendId") Long attendId);

  // ── ATTD-04 학생 본인 출석 조회 ───────────────────────────────────────────────
  // JOIN FETCH로 세션·강의를 한 번에 로딩 → N+1 방지
  // 전체 강의 조회 (lectureId 생략)
  @Query("SELECT a FROM Attendance a JOIN FETCH a.attendsession s JOIN FETCH s.lecture l " +
         "WHERE a.studentCode = :studentCode ORDER BY l.lectureId, s.classDate DESC")
  List<Attendance> findByStudentCodeWithDetails(@Param("studentCode") Long studentCode);

  // 특정 강의 조회 (lectureId 지정)
  @Query("SELECT a FROM Attendance a JOIN FETCH a.attendsession s JOIN FETCH s.lecture l " +
         "WHERE a.studentCode = :studentCode AND l.lectureId = :lectureId ORDER BY s.classDate DESC")
  List<Attendance> findByStudentCodeAndLectureIdWithDetails(
          @Param("studentCode") Long studentCode,
          @Param("lectureId") Long lectureId);
}