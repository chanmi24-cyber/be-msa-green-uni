package com.green.member.application.status;

import com.green.common.enumcode.EnumApprovalStatus;
import com.green.member.application.major.model.AdminMajorRequestDetailDto;
import com.green.member.application.major.model.AdminMajorRequestListRes;
import com.green.member.application.status.model.AdminStatusRequestDetailDto;
import com.green.member.application.status.model.AdminStatusRequestListRes;
import com.green.member.application.status.model.StudentStatusRequestDetailRes;
import com.green.member.application.status.model.StudentStatusRequestListRes;
import com.green.member.entity.student.StatusRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StatusRequestRepository extends JpaRepository<StatusRequest, Long> {
    Optional<StatusRequest> findByRequestIdAndStudent_MemberCode(Long requestId, Long memberCode);
    boolean existsByStudent_MemberCodeAndStatus(Long memberCode, EnumApprovalStatus status);

    // 관리자 처리(승인/반려) 시 동시 처리 방지를 위한 비관적 락 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sr FROM StatusRequest sr WHERE sr.requestId = :requestId")
    Optional<StatusRequest> findByIdForUpdate(@Param("requestId") Long requestId);

    // 학생 본인 학적 변경 신청 목록 조회
    @Query(value = """
            SELECT sr.request_id       AS requestId,
                   sr.type             AS type,
                   sr.status           AS status,
                   sr.academic_year    AS academicYear,
                   sr.semester         AS semester,
                   sr.return_year      AS returnYear,
                   sr.return_semester  AS returnSemester,
                   sr.created_at       AS createdAt
            FROM status_request sr
            WHERE sr.student_code = :memberCode
            ORDER BY sr.created_at DESC
            """, nativeQuery = true)
    List<StudentStatusRequestListRes> findStudentStatusRequests(
            @Param("memberCode") Long memberCode);

    // 학생 본인 학적 변경 신청 상세 조회
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
                   sr.created_at          AS createdAt,
                   sr.updated_at          AS updatedAt
            FROM status_request sr
            WHERE sr.request_id  = :requestId
              AND sr.student_code = :memberCode
            """, nativeQuery = true)
    Optional<StudentStatusRequestDetailRes> findStudentStatusRequestDetail(
            @Param("requestId")  Long requestId,
            @Param("memberCode") Long memberCode);

    // 관리자 학적 변경 신청 목록 조회 (status/search 필터 + 페이지네이션)
    @Query(value = """
            SELECT sr.request_id       AS requestId,
                   ms.member_code       AS memberCode,
                   ms.name              AS studentName,
                   ma.name                      AS updaterName,
                   CAST(sr.updater_code AS CHAR) AS updaterCode,
                   sr.type                      AS type,
                   sr.status           AS status,
                   sr.academic_year    AS academicYear,
                   sr.semester         AS semester,
                   sr.created_at       AS createdAt
            FROM status_request sr
            JOIN member ms      ON ms.member_code = sr.student_code
            LEFT JOIN member ma ON ma.member_code = sr.updater_code
            WHERE (:status IS NULL OR sr.status = :status)
              AND (:search IS NULL OR ms.name LIKE CONCAT('%', :search, '%'))
            ORDER BY sr.created_at DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM status_request sr
            JOIN member ms ON ms.member_code = sr.student_code
            WHERE (:status IS NULL OR sr.status = :status)
              AND (:search IS NULL OR ms.name LIKE CONCAT('%', :search, '%'))
            """,
            nativeQuery = true)
    Page<AdminStatusRequestListRes> findAllByFilter(
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable
    );
    // 관리자 학적 변경 신청 상세 조회
    @Query(value = """
            SELECT sr.request_id          AS requestId,
                   m.member_code          AS memberCode,
                   m.name                 AS studentName,
                   m.tel                  AS phone,
                   m.email                AS email,
                   sr.type                AS type,
                   sr.status              AS status,
                   sr.reason              AS reason,
                   sr.file                AS file,
                   sr.academic_year       AS academicYear,
                   sr.semester            AS semester,
                   sr.return_year         AS returnYear,
                   sr.return_semester     AS returnSemester,
                   sr.original_file_name  AS originalFileName,
                   sr.reject_reason       AS rejectReason,
                   um.name                       AS updaterName,
                   CAST(sr.updater_code AS CHAR)  AS updaterCode,
                   sr.start_date                  AS startDate,
                   sr.created_at          AS createdAt,
                   sr.updated_at          AS updatedAt,
                   sr.total_credits           AS totalCredits,
                   s.status                  AS academicStatus,
                   mc_current.name           AS currentMajorName,
                   mc_minor.name             AS currentMinorName
            FROM status_request sr
            JOIN member m           ON m.member_code  = sr.student_code
            JOIN student s          ON s.member_code  = sr.student_code
            LEFT JOIN member um     ON um.member_code = sr.updater_code
            LEFT JOIN student_major sm_current ON sm_current.student_code = sr.student_code
                                             AND sm_current.is_active = 1
                                             AND sm_current.type = 'PRIMARY'
            LEFT JOIN major_cache mc_current   ON mc_current.major_id = sm_current.major_id
            LEFT JOIN student_major sm_minor   ON sm_minor.student_code = sr.student_code
                                             AND sm_minor.is_active = 1
                                             AND sm_minor.type = 'MINOR'
            LEFT JOIN major_cache mc_minor     ON mc_minor.major_id = sm_minor.major_id
            WHERE sr.request_id = :requestId
            """, nativeQuery = true)
    Optional<AdminStatusRequestDetailDto> findDetailByRequestId(@Param("requestId") Long requestId);
}
