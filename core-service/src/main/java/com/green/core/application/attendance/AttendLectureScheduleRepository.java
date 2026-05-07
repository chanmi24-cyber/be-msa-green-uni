package com.green.core.application.attendance;

import com.green.core.entity.lecture.LectureSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AttendLectureScheduleRepository extends JpaRepository<LectureSchedule, Long> {

    @Query("SELECT ls FROM LectureSchedule ls JOIN FETCH ls.classRoom WHERE ls.lecture.lectureId = :lectureId")
    List<LectureSchedule> findByLectureIdWithRoom(@Param("lectureId") Long lectureId);
}