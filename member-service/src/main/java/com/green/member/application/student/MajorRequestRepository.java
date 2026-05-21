package com.green.member.application.student;

import com.green.common.enumcode.EnumApprovalStatus;
import com.green.member.application.admin.model.MajorRequestDetailDto;
import com.green.member.application.admin.model.MajorRequestRes;
import com.green.member.entity.student.MajorRequest;
import com.green.member.enumcode.EnumMajorRequestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MajorRequestRepository extends JpaRepository<MajorRequest, Long> {
    Optional<MajorRequest> findByRequestIdAndStudent_MemberCode(Long requestId, Long memberCode);
    boolean existsByStudent_MemberCodeAndTypeAndStatus(
            Long memberCode, EnumMajorRequestType type, EnumApprovalStatus status);
    List<MajorRequest> findByStudent_MemberCodeOrderByCreatedAtDesc(Long memberCode);

    // 관리자 전공 변경 신청 목록 조회 (type, status 미입력 시 전체 조회)
    @Query(value = """
            SELECT mr.request_id       AS requestId,
                   m.member_code       AS memberCode,
                   m.name              AS studentName,
                   mc.name             AS targetMajorName,
                   mr.type             AS type,
                   mr.status           AS status,
                   mr.created_at       AS createdAt
            FROM major_request mr
            JOIN member m      ON m.member_code = mr.student_code
            JOIN major_cache mc ON mc.major_id  = mr.target_major_id
            ORDER BY mr.created_at DESC
            """, nativeQuery = true)
    List<MajorRequestRes> findAllByFilter();

    // 관리자 전공 변경 신청 상세 조회
    // updater_code가 없을 수 있어서 LEFT JOIN으로 처리관리자 정보 조회
    @Query(value = """
            SELECT mr.request_id          AS requestId,
                   m.member_code          AS memberCode,
                   m.name                 AS studentName,
                   mc.name                AS targetMajorName,
                   mr.type                AS type,
                   mr.status              AS status,
                   mr.gpa                 AS gpa,
                   mr.reason              AS reason,
                   mr.file                AS file,
                   mr.original_file_name  AS originalFileName,
                   mr.approve_reason      AS approveReason,
                   mr.reject_reason       AS rejectReason,
                   um.name                AS updaterName,
                   mr.created_at          AS createdAt
            FROM major_request mr
            JOIN member m       ON m.member_code  = mr.student_code
            JOIN major_cache mc  ON mc.major_id   = mr.target_major_id
            LEFT JOIN member um  ON um.member_code = mr.updator_code
            WHERE mr.request_id = :requestId
            """, nativeQuery = true)
    Optional<MajorRequestDetailDto> findDetailByRequestId(@Param("requestId") Long requestId);
}
