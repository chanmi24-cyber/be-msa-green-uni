package com.green.member.application.status;

import com.green.common.enumcode.EnumApprovalStatus;
import com.green.member.entity.student.StatusRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatusRequestRepository extends JpaRepository<StatusRequest, Long> {
    Optional<StatusRequest> findByRequestIdAndStudent_MemberCode(Long requestId, Long memberCode);
    boolean existsByStudent_MemberCodeAndStatus(Long memberCode, EnumApprovalStatus status);
}
