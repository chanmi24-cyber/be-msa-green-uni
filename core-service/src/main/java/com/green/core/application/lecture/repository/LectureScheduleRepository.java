package com.green.core.application.lecture.repository;

import com.green.core.entity.lecture.Lecture;
import com.green.core.entity.lecture.LectureSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureScheduleRepository extends JpaRepository<LectureSchedule, Long> {
    void deleteAllByLecture(Lecture lecture);
}
