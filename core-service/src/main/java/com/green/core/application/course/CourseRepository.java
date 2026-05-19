package com.green.core.application.course;

import com.green.core.entity.course.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    // API-ENRL-03: 중복 신청 확인 — [수정] 소프트 삭제된 건 중복 아님
    boolean existsByStudentCodeAndLecture_LectureIdAndYearAndSemesterAndIsDeletedFalse(
            Long studentCode, Long lectureId, Integer year, Integer semester
    );

    // API-ENRL-03: 수강 정원 현재 인원 조회 — [수정] 소프트 삭제된 건 정원에서 제외
    int countByLecture_LectureIdAndYearAndSemesterAndIsDeletedFalse(
            Long lectureId, Integer year, Integer semester
    );

    // API-ENRL-03: 학기 총 신청 학점 합산 — [수정] 소프트 삭제된 건 학점에서 제외
    @Query("""
            SELECT COALESCE(SUM(l.credit), 0)
            FROM Course c
            JOIN c.lecture l
            WHERE c.studentCode = :studentCode
              AND c.year = :year
              AND c.semester = :semester
              AND c.isDeleted = false
            """)
    int sumCreditByStudentCodeAndYearAndSemester(
            @Param("studentCode") Long studentCode,
            @Param("year") Integer year,
            @Param("semester") Integer semester
    );

    // API-ENRL-03: 시간표 중복 확인 — [수정] 소프트 삭제된 건 시간표에서 제외
    @Query("""
            SELECT COUNT(c) > 0
            FROM Course c
            JOIN c.lecture l
            JOIN LectureSchedule ls ON ls.lecture.lectureId = l.lectureId
            WHERE c.studentCode = :studentCode
              AND c.year = :year
              AND c.semester = :semester
              AND c.isDeleted = false
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

    // API-ENRL-04: 수강 취소 시 본인 강의 조회 — [수정] 소프트 삭제된 건 재취소 불가
    Optional<Course> findByStudentCodeAndLecture_LectureIdAndYearAndSemesterAndIsDeletedFalse(
            Long studentCode, Long lectureId, Integer year, Integer semester
    );

    // API-ENRL-02: 내 수강 신청 목록 조회 — [수정] 소프트 삭제 제외
    List<Course> findByStudentCodeAndYearAndSemesterAndIsDeletedFalse(
            Long studentCode, Integer year, Integer semester
    );

    // ENRL-02 수강신청 목록 조회 — [수정] 소프트 삭제 제외
    Optional<Course> findByStudentCodeAndLecture_LectureIdAndIsDeletedFalse(Long studentCode, Long lectureId);
}