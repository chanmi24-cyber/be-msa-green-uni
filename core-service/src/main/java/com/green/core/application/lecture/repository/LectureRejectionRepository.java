package com.green.core.application.lecture.repository;

import com.green.core.entity.lecture.LectureRejection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureRejectionRepository extends JpaRepository<LectureRejection, Long> {
}
