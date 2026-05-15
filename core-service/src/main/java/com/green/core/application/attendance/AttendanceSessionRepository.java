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
}
