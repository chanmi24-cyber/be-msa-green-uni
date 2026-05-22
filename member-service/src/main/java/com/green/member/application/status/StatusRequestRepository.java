package com.green.member.application.status;

import com.green.member.entity.student.StatusRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRequestRepository extends JpaRepository<StatusRequest, Long> {
}
