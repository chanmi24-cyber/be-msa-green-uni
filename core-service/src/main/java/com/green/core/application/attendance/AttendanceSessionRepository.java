package com.green.core.application.attendance;

import com.green.core.entity.attendance.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {

    boolean existsByLecture_LectureIdAndClassDateAndIsActiveTrue(Long lectureId, LocalDate classDate);

    java.util.Optional<AttendanceSession> findByLecture_LectureIdAndIsActiveTrue(Long lectureId);
}
