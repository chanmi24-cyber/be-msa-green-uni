package com.green.core.application.tuition;

import com.green.core.entity.tuition.Tuition;
import com.green.core.enumcode.EnumTuitionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TuitionRepository extends JpaRepository<Tuition, Long> {

    Page<Tuition> findByStudentCodeOrderByYearDescSemesterDesc(Long studentCode, Pageable pageable);
    Optional<Tuition> findByStudentCodeAndYearAndSemester(Long studentCode, Integer year, Integer semester);
    Page<Tuition> findByYearAndSemesterAndStatus(Integer year, Integer semester, EnumTuitionStatus status, Pageable pageable);
    Page<Tuition> findByYearAndSemester(Integer year, Integer semester, Pageable pageable);
    List<Tuition> findByYearAndSemesterAndStatus(Integer year, Integer semester, EnumTuitionStatus status);

    // 💡 [관리자용 동적 동기화 쿼리 추가]
    // 학번 검색(studentCode)이나 이름 검색 결과(studentCodes)가 있을 때 유연하게 필터링하기 위한 쿼리입니다.
    @Query("SELECT t FROM Tuition t WHERE t.year = :year AND t.semester = :semester " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:studentCode IS NULL OR t.studentCode = :studentCode) " +
            "AND (:studentCodes IS NULL OR t.studentCode IN :studentCodes)")
    Page<Tuition> findTuitionWithFilters(
            @Param("year") Integer year,
            @Param("semester") Integer semester,
            @Param("status") EnumTuitionStatus status,
            @Param("studentCode") Long studentCode,
            @Param("studentCodes") List<Long> studentCodes,
            Pageable pageable
    );
}