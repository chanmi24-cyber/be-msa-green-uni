package com.green.core.application.tuition;

import com.green.core.entity.tuition.TuitionPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TuitionPolicyRepository extends JpaRepository<TuitionPolicy, Long> {
    // 단과대 ID 기반 조회 규칙
    Optional<TuitionPolicy> findByCollegeCollegeId(Long collegeId);
}