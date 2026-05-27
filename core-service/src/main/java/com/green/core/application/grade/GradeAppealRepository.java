package com.green.core.application.grade;

import com.green.core.entity.grade.GradesAppeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GradeAppealRepository extends JpaRepository<GradesAppeal, Long> {

    @Query("SELECT a FROM GradesAppeal a " +
           "JOIN FETCH a.grade g " +
           "JOIN FETCH g.course c " +
           "JOIN FETCH c.lecture l " +
           "WHERE a.memberCode = :memberCode " +
           "ORDER BY a.createdAt DESC")
    List<GradesAppeal> findByMemberCodeWithLecture(@Param("memberCode") Long memberCode);
}