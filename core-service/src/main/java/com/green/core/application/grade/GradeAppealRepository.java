package com.green.core.application.grade;

import com.green.core.entity.grade.GradesAppeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface GradeAppealRepository extends JpaRepository<GradesAppeal, Long> {

    @Query("SELECT a FROM GradesAppeal a " +
           "JOIN FETCH a.grade g " +
           "JOIN FETCH g.course c " +
           "JOIN FETCH c.lecture l " +
           "WHERE a.memberCode = :memberCode " +
           "ORDER BY a.createdAt DESC")
    List<GradesAppeal> findByMemberCodeWithLecture(@Param("memberCode") Long memberCode);

    // 교수 담당 강의 전체 이의신청 목록
    @Query(
        value = "SELECT a FROM GradesAppeal a " +
                "JOIN FETCH a.grade g " +
                "JOIN FETCH g.course c " +
                "JOIN FETCH c.lecture l " +
                "WHERE l.memberCode = :professorCode " +
                "ORDER BY a.createdAt DESC",
        countQuery = "SELECT COUNT(a) FROM GradesAppeal a " +
                     "JOIN a.grade g JOIN g.course c JOIN c.lecture l " +
                     "WHERE l.memberCode = :professorCode"
    )
    Page<GradesAppeal> findByProfessorCodeWithDetails(@Param("professorCode") Long professorCode, Pageable pageable);

    // 특정 이의신청 상세 조회 (FETCH JOIN 포함)
    @Query("SELECT a FROM GradesAppeal a " +
           "JOIN FETCH a.grade g " +
           "JOIN FETCH g.course c " +
           "JOIN FETCH c.lecture l " +
           "WHERE a.courseId = :courseId")
    Optional<GradesAppeal> findByIdWithDetails(@Param("courseId") Long courseId);
}