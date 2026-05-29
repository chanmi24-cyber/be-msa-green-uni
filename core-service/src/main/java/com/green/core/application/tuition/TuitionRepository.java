package com.green.core.application.tuition;

import com.green.core.entity.tuition.Tuition;
import com.green.core.enumcode.EnumTuitionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TuitionRepository extends JpaRepository<Tuition, Long> {

    // 학생: 전체납부 내역 조회
    Page<Tuition> findByStudentCodeOrderByYearDescSemesterDesc(Long studentCode, Pageable pageable);

    // 학생: 특정 학기 상세 조회 및 납부 신청용
    Optional<Tuition> findByStudentCodeAndYearAndSemester(Long studentCode, Integer year, Integer semester);

    // 관리자: 등록금 목록 조회 (학기별 페이징 및 상태 필터링)
    Page<Tuition> findByYearAndSemesterAndStatus(Integer year, Integer semester, EnumTuitionStatus status, Pageable pageable);
    Page<Tuition> findByYearAndSemester(Integer year, Integer semester, Pageable pageable);

    // 관리자: 미납자 목록 조회 (이메일 발송용)
    List<Tuition> findByYearAndSemesterAndStatus(Integer year, Integer semester, EnumTuitionStatus status);
}