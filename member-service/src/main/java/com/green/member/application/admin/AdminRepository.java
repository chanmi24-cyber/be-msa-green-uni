package com.green.member.application.admin;

import com.green.member.application.admin.model.AdminListRes;
import com.green.member.entity.member.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    @Query(
            value = """
            SELECT m.member_code   AS memberCode,
                   m.name          AS name,
                   m.email         AS email,
                   m.tel           AS tel,
                   a.status        AS status
            FROM admin a
            JOIN member m
              ON m.member_code = a.member_code
            ORDER BY m.member_code DESC
            """,
            nativeQuery = true
    )
    List<AdminListRes> findAdminList();
}
