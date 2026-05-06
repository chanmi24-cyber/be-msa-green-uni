package com.green.core.application.attendance;

import com.green.core.entity.attendance.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
}
