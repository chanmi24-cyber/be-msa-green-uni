package com.green.member.application.student;

import com.green.common.enumcode.EnumApprovalStatus;
import com.green.member.application.professor.model.ProfessorListDto;
import com.green.member.application.student.model.MajorRequestRes;
import com.green.member.entity.student.MajorRequest;
import com.green.member.enumcode.EnumMajorRequestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MajorRequestRepository extends JpaRepository<MajorRequest, Long> {
    Optional<MajorRequest> findByRequestIdAndStudent_MemberCode(Long requestId, Long memberCode);
    boolean existsByStudent_MemberCodeAndTypeAndStatus(
            Long memberCode, EnumMajorRequestType type, EnumApprovalStatus status);
    List<MajorRequest> findByStudent_MemberCodeOrderByCreatedAtDesc(Long memberCode);
}
