package com.green.core.application.course;

import com.green.core.entity.course.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    // API-ENRL-03: 중복 신청 확인
    boolean existsByStudentCodeAndLecture_LectureIdAndYearAndSemester(
            Long studentCode, Long lectureId, Integer year, Integer semester
    );

    // API-ENRL-03: 수강 정원 현재 인원 조회
    int countByLecture_LectureIdAndYearAndSemester(
            Long lectureId, Integer year, Integer semester
    );

    // API-ENRL-03: 학기 총 신청 학점 합산
    @Query("""
            SELECT COALESCE(SUM(l.credit), 0)
            FROM Course c
            JOIN c.lecture l
            WHERE c.studentCode = :studentCode
              AND c.year = :year
              AND c.semester = :semester
            """)
    int sumCreditByStudentCodeAndYearAndSemester(
            @Param("studentCode") Long studentCode,
            @Param("year") Integer year,
            @Param("semester") Integer semester
    );

    // API-ENRL-03: 시간표 중복 확인
    @Query("""
            SELECT COUNT(c) > 0
            FROM Course c
            JOIN c.lecture l
            JOIN LectureSchedule ls ON ls.lecture.lectureId = l.lectureId
            WHERE c.studentCode = :studentCode
              AND c.year = :year
              AND c.semester = :semester
              AND ls.dayOfWeek = :dayOfWeek
              AND ls.startPeriod <= :endPeriod
              AND ls.endPeriod >= :startPeriod
            """)
    boolean existsTimeConflict(
            @Param("studentCode") Long studentCode,
            @Param("year") Integer year,
            @Param("semester") Integer semester,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("startPeriod") Integer startPeriod,
            @Param("endPeriod") Integer endPeriod
    );

    // API-ENRL-04: 수강 취소 시 본인 강의 조회
    Optional<Course> findByStudentCodeAndLecture_LectureIdAndYearAndSemester(
            Long studentCode, Long lectureId, Integer year, Integer semester
    );

    // API-ENRL-02: 내 수강 신청 목록 조회
    List<Course> findByStudentCodeAndYearAndSemester(
            Long studentCode, Integer year, Integer semester
    );

    // ENRL-02 수강신청 목록 조회
    Optional<Course> findByStudentCodeAndLecture_LectureId(Long studentCode, Long lectureId);


    // 폐강 시 수강 학생 코드 목록 조회
    List<Course> findByLecture_LectureIdAndYearAndSemester(
            Long lectureId, Integer year, Integer semester
    );
}