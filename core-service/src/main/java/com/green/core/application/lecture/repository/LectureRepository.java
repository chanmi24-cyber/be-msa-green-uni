package com.green.core.application.lecture.repository;

import com.green.common.enumcode.EnumApprovalStatus;
import com.green.core.entity.lecture.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

//JPA를 사용하면서 기존 Mybatis가 해주던 역할임.
public interface LectureRepository extends JpaRepository<Lecture, Long> {
    List<Lecture> findByYearAndSemesterAndStatusAndIsDelFalse(
            Integer year, Integer semester, EnumApprovalStatus status
    );
}
