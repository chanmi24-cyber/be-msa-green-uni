package com.green.core.application.lecture.repository;

import com.green.core.entity.lecture.LectureHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureHistoryRepository extends JpaRepository<LectureHistory, Long> {
}