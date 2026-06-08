package com.green.member.application.major;

import com.green.common.enumcode.EnumApprovalStatus;
import com.green.member.application.major.model.AdminMajorRequestDetailDto;
import com.green.member.application.major.model.AdminMajorRequestListRes;
import com.green.member.application.major.model.AdminStudentMajorHistoryRes;
import com.green.member.application.major.model.StudentMajorHistoryRes;
import com.green.member.application.major.model.StudentMajorRequestDetailRes;
import com.green.member.application.major.model.StudentMajorRequestListRes;
import com.green.member.entity.student.MajorRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MajorRequestRepository extends JpaRepository<MajorRequest, Long> {
    Optional<MajorRequest> findByRequestIdAndStudent_MemberCode(Long requestId, Long memberCode);
    boolean existsByStudent_MemberCodeAndStatus(Long memberCode, EnumApprovalStatus status);

    // 관리자 전공 변경 신청 목록 조회 (status/search 필터 + 페이지네이션)
    @Query(value = """
            SELECT mr.request_id       AS requestId,
                   ms.member_code       AS memberCode,
                   ms.name              AS studentName,
                   ma.name              AS updaterName,
                   mr.updater_code     AS updaterCode,
                   mc.name             AS targetMajorName,
                   mc_current.name     AS currentMajorName,
                   mc_minor.name       AS currentMinorName,
                   mr.type             AS type,
                   mr.status           AS status,
                   mr.academic_year    AS academicYear,
                   mr.semester         AS semester,
                   mr.created_at       AS createdAt
            FROM major_request mr
            JOIN member ms      ON ms.member_code = mr.student_code
            LEFT JOIN member ma ON ma.member_code = mr.updater_code
            JOIN major_cache mc ON mc.major_id  = mr.target_major_id
            JOIN major_cache mc_current ON mc_current.major_id = mr.current_major_id
            LEFT JOIN major_cache mc_minor ON mc_minor.major_id = mr.current_minor_id
            WHERE (:status IS NULL OR mr.status = :status)
              AND (:search IS NULL OR ms.name LIKE CONCAT('%', :search, '%'))
            ORDER BY mr.created_at DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM major_request mr
            JOIN member ms ON ms.member_code = mr.student_code
            WHERE (:status IS NULL OR mr.status = :status)
              AND (:search IS NULL OR ms.name LIKE CONCAT('%', :search, '%'))
            """,
            nativeQuery = true)
    Page<AdminMajorRequestListRes> findAllByFilter(
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable
    );

    // 관리자 전공 변경 신청 상세 조회
    @Query(value = """
            SELECT mr.request_id          AS requestId,
                   m.member_code          AS memberCode,
                   m.name                 AS studentName,
                   m.tel                  AS phone,
                   m.email                AS email,
                   mc.name                AS targetMajorName,
                   mc_current.name        AS currentMajorName,
                   mc_minor.name          AS currentMinorName,
                   mr.type                AS type,
                   mr.status              AS status,
                   mr.gpa                 AS gpa,
                   mr.reason              AS reason,
                   mr.file                AS file,
                   mr.academic_year       AS academicYear,
                   mr.semester            AS semester,
                   mr.original_file_name  AS originalFileName,
                   mr.reject_reason       AS rejectReason,
                   um.name                AS updaterName,
                   mr.updater_code        AS updaterCode,
                   s.status               AS academicStatus,
                   mr.created_at          AS createdAt,
                   mr.updated_at          AS updatedAt
            FROM major_request mr
            JOIN member m          ON m.member_code  = mr.student_code
            JOIN student s         ON s.member_code  = mr.student_code
            JOIN major_cache mc     ON mc.major_id    = mr.target_major_id
            JOIN major_cache mc_current ON mc_current.major_id = mr.current_major_id
            LEFT JOIN major_cache mc_minor ON mc_minor.major_id = mr.current_minor_id
            LEFT JOIN member um    ON um.member_code  = mr.updater_code
            WHERE mr.request_id = :requestId
            """, nativeQuery = true)
    Optional<AdminMajorRequestDetailDto> findDetailByRequestId(@Param("requestId") Long requestId);

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
    List<StudentMajorRequestListRes> findStudentMajorRequests(
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
    Optional<StudentMajorRequestDetailRes> findStudentMajorRequestDetail(
            @Param("requestId")  Long requestId,
            @Param("memberCode") Long memberCode);

    // 학생 전공 변경 이력 조회
    @Query(value = """
            SELECT mr.type                                              AS type,
                   CASE
                       WHEN mr.type = 'TRANSFER' THEN mc_current.name
                       WHEN mr.type = 'MINOR'    THEN mc_minor.name
                   END                                                 AS beforeName,
                   mc_target.name                                      AS afterName,
                   mr.academic_year                                    AS academicYear,
                   mr.semester                                         AS semester,
                   mr.updated_at                                       AS updatedAt
            FROM major_request mr
            JOIN major_cache mc_current ON mc_current.major_id = mr.current_major_id
            JOIN major_cache mc_target  ON mc_target.major_id  = mr.target_major_id
            LEFT JOIN major_cache mc_minor ON mc_minor.major_id = mr.current_minor_id
            WHERE mr.student_code = :memberCode
              AND mr.status = 'APPROVED'
            ORDER BY mr.updated_at ASC
            """, nativeQuery = true)
    List<StudentMajorHistoryRes> findMajorHistoryByStudentCode(@Param("memberCode") Long memberCode);

    // 학생 전공 변경 이력 조회 (관리자용)
    @Query(value = """
            SELECT mr.type                                              AS type,
                   CASE
                       WHEN mr.type = 'TRANSFER' THEN mc_current.name
                       WHEN mr.type = 'MINOR'    THEN mc_minor.name
                   END                                                 AS beforeName,
                   mc_target.name                                      AS afterName,
                   mr.academic_year                                    AS academicYear,
                   mr.semester                                         AS semester,
                   m.name                                              AS updaterName,
                   mr.updated_at                                       AS updatedAt
            FROM major_request mr
            JOIN major_cache mc_current ON mc_current.major_id = mr.current_major_id
            JOIN major_cache mc_target  ON mc_target.major_id  = mr.target_major_id
            LEFT JOIN major_cache mc_minor ON mc_minor.major_id = mr.current_minor_id
            LEFT JOIN member m           ON m.member_code = mr.updater_code
            WHERE mr.student_code = :memberCode
              AND mr.status = 'APPROVED'
            ORDER BY mr.updated_at ASC
            """, nativeQuery = true)
    List<AdminStudentMajorHistoryRes> findMajorHistoryByStudentCodeForAdmin(@Param("memberCode") Long memberCode);
}
