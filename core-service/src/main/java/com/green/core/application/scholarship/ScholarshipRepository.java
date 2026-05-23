package com.green.core.application.scholarship;

import com.green.core.entity.scholarship.Scholarship;
import com.green.core.entity.scholarship.ScholarshipType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScholarshipRepository extends JpaRepository<Scholarship, Long> {

    boolean existsByStudentCodeAndScholarshipTypeAndYearAndSemester(
            Long studentCode, ScholarshipType scholarshipType, Integer year, Integer semester
    );

    Page<Scholarship> findAllByStudentCode(Long studentCode, Pageable pageable);
    List<Scholarship> findByStudentCodeAndYearAndSemester(Long studentCode, Integer year, Integer semester);

    @Query("""
    SELECT s FROM Scholarship s
    WHERE s.year = :year AND s.semester = :semester
""")
    Page<Scholarship> findAllByYearAndSemester(
            @Param("year") Integer year,
            @Param("semester") Integer semester,
            Pageable pageable);
}