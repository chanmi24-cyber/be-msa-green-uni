package com.green.member.application.admin;

import com.green.member.application.admin.model.AdminListRes;
import com.green.member.entity.member.Admin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    // 관리자 계정 목록 조회 (status/search 필터 + 페이지네이션)
    @Query(
            value = """
            SELECT m.member_code AS memberCode,
                   m.name        AS name,
                   m.email       AS email,
                   m.tel         AS tel,
                   a.status      AS status
            FROM admin a
            JOIN member m
              ON m.member_code = a.member_code
            WHERE (:status IS NULL OR a.status = :status)
              AND (:search IS NULL OR m.name LIKE CONCAT('%', :search, '%'))
            ORDER BY m.member_code DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM admin a
            JOIN member m
              ON m.member_code = a.member_code
            WHERE (:status IS NULL OR a.status = :status)
              AND (:search IS NULL OR m.name LIKE CONCAT('%', :search, '%'))
            """,
            nativeQuery = true
    )
    Page<AdminListRes> findAdminList(
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable
    );
}