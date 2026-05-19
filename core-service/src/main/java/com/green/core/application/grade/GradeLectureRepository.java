package com.green.core.application.grade;

import com.green.common.enumcode.EnumApprovalStatus;
import com.green.core.entity.lecture.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// [추가] 교수 강의 소유권 확인용 — lecture 테이블 읽기 전용
public interface GradeLectureRepository extends JpaRepository<Lecture, Long> {

    // lectureId + memberCode(교수코드) 일치 여부로 본인 강의 확인
    Optional<Lecture> findByLectureIdAndMemberCode(Long lectureId, Long memberCode);

    // [추가] 교수 본인의 승인된 강의 목록 (성적 관리 강의 선택 화면용)
    List<Lecture> findByMemberCodeAndStatusAndIsDelFalse(Long memberCode, EnumApprovalStatus status);
}