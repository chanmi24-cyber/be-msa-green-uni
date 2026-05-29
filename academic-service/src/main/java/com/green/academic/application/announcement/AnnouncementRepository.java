package com.green.academic.application.announcement;

import com.green.academic.entity.Announcement;
import com.green.academic.enumcode.EnumTargetRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    // 관리자: 본인 작성 + targetRole 선택 필터
    @Query(value = "SELECT a FROM Announcement a WHERE a.memberCode = :memberCode AND a.isDel = false " +
                   "AND (:targetRole IS NULL OR a.targetRole = :targetRole) ORDER BY a.createdAt DESC",
           countQuery = "SELECT COUNT(a) FROM Announcement a WHERE a.memberCode = :memberCode AND a.isDel = false " +
                        "AND (:targetRole IS NULL OR a.targetRole = :targetRole)")
    Page<Announcement> findByAdmin(@Param("memberCode") Long memberCode,
                                   @Param("targetRole") EnumTargetRole targetRole,
                                   Pageable pageable);

    // 학생/교수: 역할별 대상 공지만 조회
    @Query(value = "SELECT a FROM Announcement a WHERE a.targetRole = :targetRole AND a.isDel = false ORDER BY a.createdAt DESC",
           countQuery = "SELECT COUNT(a) FROM Announcement a WHERE a.targetRole = :targetRole AND a.isDel = false")
    Page<Announcement> findByTargetRole(@Param("targetRole") EnumTargetRole targetRole, Pageable pageable);

    // 상세조회 (삭제되지 않은 것만)
    Optional<Announcement> findByAnnoIdAndIsDelFalse(Long annoId);
}