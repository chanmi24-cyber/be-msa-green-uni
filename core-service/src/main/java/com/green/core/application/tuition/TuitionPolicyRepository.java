package com.green.core.application.tuition;

import com.green.core.entity.tuition.TuitionPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TuitionPolicyRepository extends JpaRepository<TuitionPolicy, Long> {
    Optional<TuitionPolicy> findByYearAndSemesterAndCollegeCollegeId(Integer year, Integer semester, Long collegeId);
}