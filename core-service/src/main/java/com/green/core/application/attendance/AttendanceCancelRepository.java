package com.green.core.application.attendance;

import com.green.core.entity.attendance.AttendanceCancel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// [추가] 휴강/보강 이력 관리용 Repository
public interface AttendanceCancelRepository extends JpaRepository<AttendanceCancel, Long> {

    // 특정 강의의 휴강 내역 전체 조회 (최신순)
    List<AttendanceCancel> findByLecture_LectureIdOrderByCancelDateDesc(Long lectureId);

    // 특정 날짜의 휴강 기록 조회 — 보강 세션 시작 시 makeupDate 업데이트용
    Optional<AttendanceCancel> findByLecture_LectureIdAndCancelDate(Long lectureId, LocalDate cancelDate);
}