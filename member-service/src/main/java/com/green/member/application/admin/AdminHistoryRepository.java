package com.green.member.application.admin;

import com.green.member.entity.member.AdminHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminHistoryRepository extends JpaRepository<AdminHistory, Long> {
}
