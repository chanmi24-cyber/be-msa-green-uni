package com.green.core.application.attendance;

import com.green.core.entity.lecture.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendLectureRepository extends JpaRepository<Lecture, Long> {
}