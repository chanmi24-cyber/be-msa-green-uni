package com.green.member.application.status;

import com.green.common.enumcode.EnumApprovalStatus;
import com.green.member.application.status.model.StudentStatusRequestDetailRes;
import com.green.member.application.status.model.StudentStatusRequestListRes;
import com.green.member.entity.student.StatusRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StatusRequestRepository extends JpaRepository<StatusRequest, Long> {
    Optional<StatusRequest> findByRequestIdAndStudent_MemberCode(Long requestId, Long memberCode);
    boolean existsByStudent_MemberCodeAndStatus(Long memberCode, EnumApprovalStatus status);

    // 학생 본인 학적 변경 신청 목록 조회
    @Query(value = """
            SELECT sr.request_id       AS requestId,
                   sr.type             AS type,
                   sr.status           AS status,
                   sr.academic_year    AS academicYear,
                   sr.semester         AS semester,
                   sr.created_at       AS createdAt
            FROM status_request sr
            WHERE sr.student_code = :memberCode
            ORDER BY sr.created_at DESC
            """, nativeQuery = true)
    List<StudentStatusRequestListRes> findStudentStatusRequests(
            @Param("memberCode") Long memberCode);

    // 학생 본인 전공 변경 신청 상세 조회
    @Query(value = """
            SELECT sr.request_id          AS requestId,
                   sr.type                AS type,
                   sr.status              AS status,
                   sr.reason              AS reason,
                   sr.file                AS file,
                   sr.original_file_name  AS originalFileName,
                   sr.reject_reason       AS rejectReason,
                   sr.academic_year       AS academicYear,
                   sr.semester            AS semester,
                   sr.start_date          AS startDate,
                   sr.return_year         AS returnYear,
                   sr.return_semester     AS returnSemester,
                   sr.created_at          AS createdAt
            FROM status_request sr
            WHERE sr.request_id  = :requestId
              AND sr.student_code = :memberCode
            """, nativeQuery = true)
    Optional<StudentStatusRequestDetailRes> findStudentStatusRequestDetail(
            @Param("requestId")  Long requestId,
            @Param("memberCode") Long memberCode);
}
