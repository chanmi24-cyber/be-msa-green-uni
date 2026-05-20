package com.green.core.application.tuition;

import com.green.core.entity.tuition.TuitionPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TuitionPolicyRepository extends JpaRepository<TuitionPolicy, Long> {
    Optional<TuitionPolicy> findByYearAndSemesterAndCollegeCollegeId(Integer year, Integer semester, Long collegeId);

    // 스케줄러 자동 생성 전 검증 및 목록 조회용
    List<TuitionPolicy> findByYearAndSemester(Integer year, Integer semester);
}