package com.green.member.application.student;

import com.green.common.enumcode.EnumApprovalStatus;
import com.green.member.entity.student.MajorRequest;
import com.green.member.enumcode.EnumMajorRequestType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MajorRequestRepository extends JpaRepository<MajorRequest, Long> {
    Optional<MajorRequest> findByRequestIdAndStudent_MemberCode(Long requestId, Long memberCode);
    boolean existsByStudent_MemberCodeAndTypeAndStatus(
            Long memberCode, EnumMajorRequestType type, EnumApprovalStatus status);
}
