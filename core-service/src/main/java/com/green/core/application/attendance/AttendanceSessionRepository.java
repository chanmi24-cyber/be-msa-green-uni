package com.green.core.application.attendance;

import com.green.core.entity.attendance.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {

    boolean existsByLecture_LectureIdAndClassDate(Long lectureId, LocalDate classDate);

    Optional<AttendanceSession> findByLecture_LectureIdAndIsActiveTrue(Long lectureId);

    Optional<AttendanceSession> findByLecture_LectureIdAndClassDate(Long lectureId, java.time.LocalDate classDate);

    @Query("SELECT s FROM AttendanceSession s JOIN FETCH s.lecture WHERE s.isActive = true")
    List<AttendanceSession> findAllActiveWithLecture();

    // [추가] 시작 후 15분이 지난 활성 세션 조회 — 스케줄러 자동 종료용
    @Query("SELECT s FROM AttendanceSession s JOIN FETCH s.lecture WHERE s.isActive = true AND s.startedAt < :cutoff")
    List<AttendanceSession> findAllActiveStartedBefore(@org.springframework.data.repository.query.Param("cutoff") java.time.LocalDateTime cutoff);

    List<AttendanceSession> findByLecture_LectureIdOrderByClassDateDesc(Long lectureId);

    // [추가] 학생 출석 이력 조회: 여러 강의의 전체 세션 + 해당 학생의 출석 기록 LEFT JOIN
    // CANCEL 세션은 Attendance가 없으므로 LEFT JOIN 필수 — ON 절에서 studentCode 필터
    @Query("SELECT s, a FROM AttendanceSession s " +
           "LEFT JOIN Attendance a ON a.attendsession = s AND a.studentCode = :studentCode " +
           "WHERE s.lecture.lectureId IN :lectureIds " +
           "ORDER BY s.lecture.lectureId ASC, s.classDate ASC")
    List<Object[]> findSessionsWithAttendance(
            @org.springframework.data.repository.query.Param("lectureIds") List<Long> lectureIds,
            @org.springframework.data.repository.query.Param("studentCode") Long studentCode);
}
