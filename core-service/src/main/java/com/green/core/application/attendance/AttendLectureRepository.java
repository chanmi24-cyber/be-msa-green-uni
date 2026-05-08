package com.green.core.application.attendance;

import com.green.common.enumcode.EnumApprovalStatus;
import com.green.core.entity.lecture.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendLectureRepository extends JpaRepository<Lecture, Long> {

    List<Lecture> findByMemberCodeAndIsDelFalseAndStatus(Long memberCode, EnumApprovalStatus status);
}