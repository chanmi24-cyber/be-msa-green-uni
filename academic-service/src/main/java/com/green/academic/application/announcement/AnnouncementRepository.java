package com.green.academic.application.announcement;

import com.green.academic.entity.Announcement;
import com.green.academic.enumcode.EnumTargetRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    // 관리자: 전체 조회 (필터 없음)
    Page<Announcement> findAllByIsDelFalseOrderByCreatedAtDesc(Pageable pageable);

    // 관리자: targetRole 필터만
    Page<Announcement> findByTargetRoleAndIsDelFalseOrderByCreatedAtDesc(
            EnumTargetRole targetRole, Pageable pageable);

    // 관리자: 제목 검색만
    @Query("SELECT a FROM Announcement a WHERE a.isDel = false " +
           "AND a.title LIKE %:search% ORDER BY a.createdAt DESC")
    Page<Announcement> findAllBySearchAndIsDelFalse(
            @Param("search") String search, Pageable pageable);

    // 관리자: targetRole 필터 + 제목 검색
    @Query(value = "SELECT a FROM Announcement a WHERE a.isDel = false " +
                   "AND a.targetRole = :targetRole AND a.title LIKE %:search% ORDER BY a.createdAt DESC",
           countQuery = "SELECT COUNT(a) FROM Announcement a WHERE a.isDel = false " +
                        "AND a.targetRole = :targetRole AND a.title LIKE %:search%")
    Page<Announcement> findByTargetRoleAndSearchAndIsDelFalse(
            @Param("targetRole") EnumTargetRole targetRole,
            @Param("search") String search,
            Pageable pageable);

    // 학생/교수: 본인 역할 + ALL 공지 조회
    Page<Announcement> findByTargetRoleInAndIsDelFalseOrderByCreatedAtDesc(
            List<EnumTargetRole> targetRoles, Pageable pageable);

    // 상세조회 (삭제되지 않은 것만)
    Optional<Announcement> findByAnnoIdAndIsDelFalse(Long annoId);

    // 조회수 직접 증가 (updated_at 갱신 방지)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Announcement a SET a.viewCount = a.viewCount + 1 WHERE a.annoId = :annoId")
    void incrementViewCount(@Param("annoId") Long annoId);
}