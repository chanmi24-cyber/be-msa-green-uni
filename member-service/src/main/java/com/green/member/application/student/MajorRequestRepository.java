package com.green.member.application.student;

import com.green.common.enumcode.EnumApprovalStatus;
import com.green.member.entity.student.MajorRequest;
import com.green.member.enumcode.EnumMajorRequestType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MajorRequestRepository extends JpaRepository<MajorRequest, Long> {

    boolean existsByStudent_MemberCodeAndTypeAndStatus(
            Long memberCode, EnumMajorRequestType type, EnumApprovalStatus status);
}
