package com.green.core.application.course;

import com.green.core.entity.course.Course;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * [수정] 동시성 보완을 위한 변경 사항
 *
 * 1. countByLecture_LectureIdAndYearAndSemesterAndIsDelFalseWithLock
 *    - 정원 초과 확인 시 Race Condition 차단을 위해 비관적 락(PESSIMISTIC_WRITE) 적용
 *    - SELECT COUNT(...) FOR UPDATE 로 동작하여 조회~저장 사이 다른 트랜잭션 끼어들기를 막음
 *
 * 2. DB unique constraint 권고
 *    - (student_code, lecture_id, year, semester) 조합에 unique 제약 추가를 강력히 권장
 *    - 애플리케이션 레벨 중복 체크만으로는 동시 요청 시 중복 저장 가능
 *    - DDL 예시:
 *        ALTER TABLE course
 *            ADD CONSTRAINT uq_course_enrollment
 *                UNIQUE (student_code, lecture_id, year, semester);
 *    - 단, soft delete 운용 중이면 is_del=false 조건을 포함한 부분 인덱스(partial index) 사용 권장
 *        (PostgreSQL: CREATE UNIQUE INDEX ... WHERE is_del = false)
 *        (MySQL: unique constraint 대신 애플리케이션+비관적 락 조합으로 처리)
 */
public interface CourseRepository extends JpaRepository<Course, Long> {

    // API-ENRL-03: 중복 신청 확인 (활성 수강만)
    boolean existsByStudentCodeAndLecture_LectureIdAndYearAndSemesterAndIsDelFalse(
            Long studentCode, Long lectureId, Integer year, Integer semester
    );

    // API-ENRL-03: 수강 정원 현재 인원 조회 (활성 수강만)
    int countByLecture_LectureIdAndYearAndSemesterAndIsDelFalse(
            Long lectureId, Integer year, Integer semester
    );

    /**
     * [추가] 비관적 락을 적용한 정원 조회
     * - createCourse() 트랜잭션 내에서 정원 초과 확인 시 사용
     * - PESSIMISTIC_WRITE: 해당 강의의 수강 레코드에 FOR UPDATE 락을 걸어
     *   조회~저장 구간에서 다른 트랜잭션의 동시 수강 신청을 직렬화
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT COUNT(c)
            FROM Course c
            WHERE c.lecture.lectureId = :lectureId
              AND c.year = :year
              AND c.semester = :semester
              AND c.isDel = false
            """)
    int countByLecture_LectureIdAndYearAndSemesterAndIsDelFalseWithLock(
            @Param("lectureId") Long lectureId,
            @Param("year") Integer year,
            @Param("semester") Integer semester
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