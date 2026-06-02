package com.green.member.application.professor;

import com.green.member.application.professor.model.ProfessorListRes;
import com.green.member.entity.professor.Professor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    // 관리자 교수 목록 조회 (status/majorName/position/search 필터 + 페이지네이션)
    @Query(
            value = """
            SELECT m.member_code AS memberCode,
                   m.name        AS name,
                   m.email       AS email,
                   m.tel         AS tel,
                   p.status      AS status,
                   p.position    AS position,
                   mc_p.name     AS majorName
            FROM professor p
            JOIN member m
              ON m.member_code = p.member_code
            JOIN major_cache mc_p
              ON mc_p.major_id = p.major_id
            WHERE (:status IS NULL OR p.status = :status)
              AND (:majorName IS NULL OR mc_p.name = :majorName)
              AND (:position IS NULL OR p.position = :position)
              AND (:search IS NULL OR m.name LIKE CONCAT('%', :search, '%'))
            ORDER BY m.member_code DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM professor p
            JOIN member m
              ON m.member_code = p.member_code
            JOIN major_cache mc_p
              ON mc_p.major_id = p.major_id
            WHERE (:status IS NULL OR p.status = :status)
              AND (:majorName IS NULL OR mc_p.name = :majorName)
              AND (:position IS NULL OR p.position = :position)
              AND (:search IS NULL OR m.name LIKE CONCAT('%', :search, '%'))
            """,
            nativeQuery = true
    )
    Page<ProfessorListRes> findProfessorList(
            @Param("status") String status,
            @Param("majorName") String majorName,
            @Param("position") String position,
            @Param("search") String search,
            Pageable pageable
    );
}