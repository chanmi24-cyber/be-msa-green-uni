package com.green.member.application.admin;

import com.green.member.entity.member.AdminHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminHistoryRepository extends JpaRepository<AdminHistory, Long> {
    List<AdminHistory> findByAdmin_MemberCode(Long memberCode);
}
