package com.green.core.application.grade;

import com.green.core.entity.grade.GradesAppeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// [추가] 성적 이의신청 전용 Repository
public interface GradeAppealRepository extends JpaRepository<GradesAppeal, Long> {

    // 특정 grade(courseId)의 이의신청 조회
    Optional<GradesAppeal> findByCourseId(Long courseId);

    // 학생 본인 이의신청 목록
    @Query("SELECT a FROM GradesAppeal a " +
           "JOIN FETCH a.grade g " +
           "JOIN FETCH g.course c " +
           "JOIN FETCH c.lecture l " +
           "WHERE a.memberCode = :memberCode " +
           "ORDER BY a.createdAt DESC")
    List<GradesAppeal> findByMemberCode(@Param("memberCode") Long memberCode);

    // 교수 강의의 이의신청 목록
    @Query("SELECT a FROM GradesAppeal a " +
           "JOIN FETCH a.grade g " +
           "JOIN FETCH g.course c " +
           "JOIN FETCH c.lecture l " +
           "WHERE l.lectureId = :lectureId " +
           "ORDER BY a.createdAt DESC")
    List<GradesAppeal> findByLectureId(@Param("lectureId") Long lectureId);

    // 교수 이의신청 상세 (appealId = courseId)
    @Query("SELECT a FROM GradesAppeal a " +
           "JOIN FETCH a.grade g " +
           "JOIN FETCH g.course c " +
           "JOIN FETCH c.lecture l " +
           "WHERE a.courseId = :appealId")
    Optional<GradesAppeal> findDetailById(@Param("appealId") Long appealId);
}
