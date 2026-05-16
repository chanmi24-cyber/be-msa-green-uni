package com.green.core.application.lecture.repository;

import com.green.core.entity.lecture.Lecture;
import com.green.core.entity.lecture.LectureSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LectureScheduleRepository extends JpaRepository<LectureSchedule, Long> {
    @Modifying
    @Query("DELETE FROM LectureSchedule ls WHERE ls.lecture = :lecture")
    void deleteAllByLecture(Lecture lecture);

    @Query("""
    SELECT COUNT(ls) FROM LectureSchedule ls
    WHERE ls.lecture.memberCode = :memberCode
    AND ls.dayOfWeek = :dayOfWeek
    AND ls.startPeriod <= :endPeriod
    AND ls.endPeriod >= :startPeriod
    AND ls.lecture.year = :year
    AND ls.lecture.semester = :semester
    """)
    long countProfessorConflict(
            @Param("memberCode") Long memberCode,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("startPeriod") Integer startPeriod,
            @Param("endPeriod") Integer endPeriod,
            @Param("year") Integer year,
            @Param("semester") Integer semester
    );

    List<LectureSchedule> findByLecture_LectureId(Long lectureId);
}
