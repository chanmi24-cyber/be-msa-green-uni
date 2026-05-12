package com.green.core.application.lecture.repository;

import com.green.core.entity.lecture.Lecture;
import com.green.core.entity.lecture.LectureSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LectureScheduleRepository extends JpaRepository<LectureSchedule, Long> {
    @Modifying
    @Query("DELETE FROM LectureSchedule ls WHERE ls.lecture = :lecture")
    void deleteAllByLecture(Lecture lecture);

    List<LectureSchedule> findByLecture_LectureId(Long lectureId);
}
