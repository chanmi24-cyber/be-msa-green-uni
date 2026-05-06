package com.green.member.application.member;

import com.green.member.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository  extends JpaRepository<Member, Long> {
    @Query("""
        SELECT COUNT(s) + 1
        FROM Student s
        JOIN s.member m
        WHERE YEAR(m.entryDate) = :entryYear
    """)
    int countStudentByEntryYear(@Param("entryYear") int entryYear);
}
