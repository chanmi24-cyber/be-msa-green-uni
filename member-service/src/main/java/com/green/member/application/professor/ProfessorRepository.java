package com.green.member.application.professor;

import com.green.member.application.professor.model.ProfessorListRes;
import com.green.member.entity.professor.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProfessorRepository extends JpaRepository<Professor,Long> {

    @Query(
            value = """
            SELECT m.member_code   AS memberCode,
                   m.name          AS name,
                   m.email         AS email,
                   m.tel           AS tel,
                   p.status        AS status,
                   p.position       AS position,
                   mc_p.name         AS majorName
            FROM professor p
            JOIN member m
              ON m.member_code = p.member_code
            JOIN major_cache mc_p
              ON mc_p.major_id = p.major_id
            ORDER BY m.member_code DESC
            """,
            nativeQuery = true
    )
    List<ProfessorListRes> findProfessorList();
}
