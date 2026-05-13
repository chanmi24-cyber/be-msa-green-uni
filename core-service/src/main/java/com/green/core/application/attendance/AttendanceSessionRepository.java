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

    //활성 세션 전체 조회 추가
    @Query("SELECT s FROM AttendanceSession s JOIN FETCH s.lecture WHERE s.isActive = true")
    List<AttendanceSession> findAllActiveWithLecture();

    List<AttendanceSession> findByLecture_LectureIdOrderByClassDateDesc(Long lectureId);
}
