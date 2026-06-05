package com.green.member.application.member;

import com.green.member.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository  extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);

    @Query("""
        SELECT COALESCE(MAX(MOD(m.memberCode, 1000)), 0) + 1
        FROM Student s
        JOIN s.member m
        WHERE YEAR(m.entryDate) = :entryYear
    """)
    int countStudentByEntryYear(@Param("entryYear") int entryYear);

    @Query("""
        SELECT COALESCE(MAX(MOD(m.memberCode, 1000)), 0) + 1
        FROM Professor p
        JOIN p.member m
        WHERE YEAR(m.entryDate) = :entryYear
    """)
    int countProfessorByEntryYear(@Param("entryYear") int entryYear);

    @Query("""
        SELECT COALESCE(MAX(MOD(m.memberCode, 1000)), 0) + 1
        FROM Admin a
        JOIN a.member m
        WHERE YEAR(m.entryDate) = :entryYear
    """)
    int countAdminByEntryYear(@Param("entryYear") int entryYear);
}
