package com.green.core.application.lecture.repository;

import com.green.core.entity.lecture.LectureSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LectureScheduleRepository extends JpaRepository<LectureSchedule, Long> {
    List<LectureSchedule> findByLecture_LectureId(Long lectureId);
}
