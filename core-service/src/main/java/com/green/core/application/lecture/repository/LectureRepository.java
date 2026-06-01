package com.green.core.application.lecture.repository;

import com.green.common.enumcode.EnumApprovalStatus;
import com.green.core.entity.lecture.Lecture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LectureRepository extends JpaRepository<Lecture, Long> {

    // 기존 유지 (다른 곳에서 사용 중일 수 있으므로)
    List<Lecture> findByYearAndSemesterAndStatusAndIsDelFalse(
            Integer year, Integer semester, EnumApprovalStatus status
    );

    // ✅ 추가: 수강 신청 목록 조회 - 필터 + 페이징
    @Query("""
        SELECT l FROM Lecture l
        WHERE l.year = :year
          AND l.semester = :semester
          AND l.status = :status
          AND l.isDel = false
          AND (:lectureType IS NULL OR CAST(l.lectureType AS string) LIKE CONCAT(:lectureType, '%'))
          AND (:majorId IS NULL OR l.major.majorId = :majorId)
          AND (:academicYear IS NULL OR l.academicYear = :academicYear)
          AND (:search IS NULL OR l.lectureName LIKE CONCAT('%', :search, '%'))
        """)
    Page<Lecture> findByFilters(
            @Param("year") Integer year,
            @Param("semester") Integer semester,
            @Param("status") EnumApprovalStatus status,
            @Param("lectureType") String lectureType,
            @Param("majorId") Long majorId,
            @Param("academicYear") Integer academicYear,
            @Param("search") String search,
            Pageable pageable
    );
}