package com.green.core.application.grade;

import com.green.core.entity.major.Major;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GradeMajorRepository extends JpaRepository<Major, Long> {

    @Query("SELECT m FROM Major m JOIN FETCH m.college WHERE m.majorId = :majorId")
    Optional<Major> findWithCollegeById(@Param("majorId") Long majorId);
}