package com.green.core.application.scholarship;

import com.green.core.entity.tuition.ScholarshipType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScholarshipTypeRepository extends JpaRepository<ScholarshipType, Long> {

    Optional<ScholarshipType> findByScholarshipType(String scholarshipType);
}