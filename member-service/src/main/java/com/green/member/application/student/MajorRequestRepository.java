package com.green.member.application.student;

import com.green.common.enumcode.EnumApprovalStatus;
import com.green.member.application.admin.model.MajorRequestDetailDto;
import com.green.member.application.admin.model.MajorRequestRes;
import com.green.member.application.student.model.MajorRequestDetailRes;
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

    // 관리자 전공 변경 신청 목록 조회
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
                   mr.academic_year       AS academicYear,
                   mr.semester            AS semester,
                   mr.original_file_name  AS originalFileName,
                   mr.approve_reason      AS approveReason,
                   mr.reject_reason       AS rejectReason,
                   um.name                AS updaterName,
                   mr.created_at          AS createdAt
            FROM major_request mr
            JOIN member m       ON m.member_code  = mr.student_code
            JOIN major_cache mc  ON mc.major_id   = mr.target_major_id
            LEFT JOIN member um  ON um.member_code = mr.updater_code
            WHERE mr.request_id = :requestId
            """, nativeQuery = true)
    Optional<MajorRequestDetailDto> findDetailByRequestId(@Param("requestId") Long requestId);

    // 학생 본인 전공 변경 신청 목록 조회
    @Query(value = """
            SELECT mr.request_id       AS requestId,
                   mr.type             AS type,
                   mc.name             AS targetMajorName,
                   mr.status           AS status,
                   mr.academic_year    AS academicYear,
                   mr.semester         AS semester,
                   mr.created_at       AS createdAt
            FROM major_request mr
            JOIN major_cache mc ON mc.major_id = mr.target_major_id
            WHERE mr.student_code = :memberCode
            ORDER BY mr.created_at DESC
            """, nativeQuery = true)
    List<com.green.member.application.student.model.MajorRequestRes> findStudentMajorRequests(
            @Param("memberCode") Long memberCode);

    // 학생 본인 전공 변경 신청 상세 조회
    // current_minor_id가 없을 수 있어서 LEFT JOIN으로 처리
    @Query(value = """
            SELECT mr.request_id          AS requestId,
                   mr.type                AS type,
                   mc_target.name         AS targetMajorName,
                   mr.status              AS status,
                   mr.gpa                 AS gpa,
                   mr.reason              AS reason,
                   mr.file                AS file,
                   mr.original_file_name  AS originalFileName,
                   mr.approve_reason      AS approveReason,
                   mr.reject_reason       AS rejectReason,
                   mr.academic_year       AS academicYear,
                   mr.semester            AS semester,
                   mc_current.name        AS currentMajorName,
                   mc_minor.name          AS currentMinorName,
                   mr.created_at          AS createdAt
            FROM major_request mr
            JOIN major_cache mc_target  ON mc_target.major_id  = mr.target_major_id
            JOIN major_cache mc_current ON mc_current.major_id = mr.current_major_id
            LEFT JOIN major_cache mc_minor ON mc_minor.major_id = mr.current_minor_id
            WHERE mr.request_id  = :requestId
              AND mr.student_code = :memberCode
            """, nativeQuery = true)
    Optional<MajorRequestDetailRes> findStudentMajorRequestDetail(
            @Param("requestId")  Long requestId,
            @Param("memberCode") Long memberCode);
}
