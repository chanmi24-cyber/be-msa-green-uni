package com.green.core.application.course;

import com.green.core.entity.course.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    // API-ENRL-03: 중복 신청 확인 (활성 수강만)
    boolean existsByStudentCodeAndLecture_LectureIdAndYearAndSemesterAndIsDelFalse(
            Long studentCode, Long lectureId, Integer year, Integer semester
    );

    // API-ENRL-03: 수강 정원 현재 인원 조회 (활성 수강만)
    int countByLecture_LectureIdAndYearAndSemesterAndIsDelFalse(
            Long lectureId, Integer year, Integer semester
    );

    // API-ENRL-03: 학기 총 신청 학점 합산 (활성 수강만)
    @Query("""
            SELECT COALESCE(SUM(l.credit), 0)
            FROM Course c
            JOIN c.lecture l
            WHERE c.studentCode = :studentCode
              AND c.year = :year
              AND c.semester = :semester
              AND c.isDel = false
            """)
    int sumCreditByStudentCodeAndYearAndSemester(
            @Param("studentCode") Long studentCode,
            @Param("year") Integer year,
            @Param("semester") Integer semester
    );

    // API-ENRL-03: 시간표 중복 확인 (활성 수강만)
    @Query("""
            SELECT COUNT(c) > 0
            FROM Course c
            JOIN c.lecture l
            JOIN LectureSchedule ls ON ls.lecture.lectureId = l.lectureId
            WHERE c.studentCode = :studentCode
              AND c.year = :year
              AND c.semester = :semester
              AND c.isDel = false
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

    // API-ENRL-04: 수강 취소 시 활성 수강 조회
    Optional<Course> findByStudentCodeAndLecture_LectureIdAndYearAndSemesterAndIsDelFalse(
            Long studentCode, Long lectureId, Integer year, Integer semester
    );

    // API-ENRL-03: 수강정정 중 재수강신청 시 소프트딜리트된 레코드 조회
    Optional<Course> findByStudentCodeAndLecture_LectureIdAndYearAndSemesterAndIsDelTrue(
            Long studentCode, Long lectureId, Integer year, Integer semester
    );

    // API-ENRL-02: 내 수강 신청 목록 조회 (활성 수강만)
    List<Course> findByStudentCodeAndYearAndSemesterAndIsDelFalse(
            Long studentCode, Integer year, Integer semester
    );

    // ENRL-02 수강신청 목록 조회
    Optional<Course> findByStudentCodeAndLecture_LectureIdAndIsDelFalse(Long studentCode, Long lectureId);

    // 폐강 시 수강 학생 코드 목록 조회 (활성 수강만)
    List<Course> findByLecture_LectureIdAndYearAndSemesterAndIsDelFalse(
            Long lectureId, Integer year, Integer semester
    );
}
