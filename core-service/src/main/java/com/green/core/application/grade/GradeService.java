package com.green.core.application.grade;

import com.green.common.auth.MemberContext;
import com.green.common.enumcode.EnumApprovalStatus;
import com.green.common.exception.BusinessException;
import com.green.core.application.grade.model.GradeLectureListRes;
import com.green.core.application.grade.model.GradeListRes;
import com.green.core.application.grade.model.GradeUpdateReq;
import com.green.core.entity.attendance.Attendance;
import com.green.core.entity.cache.StudentCache;
import com.green.core.entity.grade.Grade;
import com.green.core.enumcode.EnumAttendStatus;
import com.green.core.enumcode.EnumGradeLetter;
import com.green.core.exception.GradeErrorCode;
import com.green.core.repository.StudentCacheRepository;
import com.green.core.scheduleValidator.SchedulePeriodValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// [추가] 성적 서비스 — 교수 성적 조회/입력 구현 (학생 성적 조회는 추후 추가)
@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository gradeRepository;
    private final GradeAttendRepository gradeAttendRepository;
    private final GradeLectureRepository gradeLectureRepository;
    private final StudentCacheRepository studentCacheRepository;
    private final SchedulePeriodValidator schedulePeriodValidator;

    // ── 교수 담당 강의 목록 조회 (성적 관리 강의 선택 화면용) ─────────────────────
    @Transactional(readOnly = true)
    public List<GradeLectureListRes> getProfessorLectures() {
        Long professorCode = MemberContext.get().memberCode();
        return gradeLectureRepository
                .findByMemberCodeAndStatusAndIsDelFalse(professorCode, EnumApprovalStatus.APPROVED)
                .stream()
                .map(l -> new GradeLectureListRes(
                        l.getLectureId(),
                        l.getLectureName(),
                        l.getLectureType().getValue(),
                        l.getYear(),
                        l.getSemester(),
                        l.getCredit(),
                        l.getAcademicYear()
                ))
                .toList();
    }

    // ── API-GPA-03: 교수 성적 조회 ────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<GradeListRes> getProfessorGrades(Long lectureId) {
        Long professorCode = MemberContext.get().memberCode();

        // G002: 본인 강의 확인
        gradeLectureRepository.findByLectureIdAndMemberCode(lectureId, professorCode)
                .orElseThrow(() -> new BusinessException(GradeErrorCode.NOT_PROFESSOR_LECTURE));

        List<Grade> grades = gradeRepository.findByLectureId(lectureId);

        return grades.stream()
                .map(grade -> {
                    Long studentCode = grade.getCourse().getStudentCode();

                    // StudentCache에서 이름·학년 조회 (없으면 빈 값)
                    StudentCache student = studentCacheRepository.findById(studentCode).orElse(null);
                    String memberName   = student != null ? student.getName()         : null;
                    Integer academicYear = student != null ? student.getAcademicYear() : null;

                    return new GradeListRes(
                            grade.getCourseId(),
                            studentCode,
                            memberName,
                            academicYear,
                            grade.getMidScore(),
                            grade.getFinScore(),
                            grade.getAssignmentScore(),
                            grade.getAttendScore(),
                            grade.getTotalScore(),
                            grade.getGradeLetter() != null ? grade.getGradeLetter().getCode() : null
                    );
                })
                .toList();
    }

    // ── API-GPA-02: 교수 성적 입력/수정 ──────────────────────────────────────
    @Transactional
    public void updateGrades(Long lectureId, List<GradeUpdateReq> reqList) {
        Long professorCode = MemberContext.get().memberCode();

        // G002: 본인 강의 확인
        gradeLectureRepository.findByLectureIdAndMemberCode(lectureId, professorCode)
                .orElseThrow(() -> new BusinessException(GradeErrorCode.NOT_PROFESSOR_LECTURE));

        for (GradeUpdateReq req : reqList) {
            // G003: 점수 범위 검증 (0~100)
            validateScore(req.getMidScore(), req.getFinScore(), req.getAssignmentScore());

            // 출석 점수 자동 계산 (attendance 테이블 집계)
            int attendScore = calcAttendScore(req.getCourseId());

            // 총점 계산: mid30% + fin30% + assignment20% + attend20%
            int totalScore = Grade.calcTotalScore(
                    req.getMidScore(), req.getFinScore(), req.getAssignmentScore(), attendScore);

            // 등급 자동 부여
            EnumGradeLetter gradeLetter = Grade.calcGradeLetter(totalScore);

            // JPQL UPDATE (dirty checking 대신 명시적 업데이트)
            gradeRepository.updateScores(
                    req.getCourseId(),
                    req.getMidScore(),
                    req.getFinScore(),
                    req.getAssignmentScore(),
                    attendScore,
                    totalScore,
                    gradeLetter
            );
        }
    }

    // ── 출석 점수 자동 계산 ───────────────────────────────────────────────────
    // 시작 100점 기준: 결석 -5점, 지각/조퇴 -2점, 최소 0점
    private int calcAttendScore(Long courseId) {
        List<Attendance> attendances = gradeAttendRepository.findByCourse_CourseId(courseId);
        long absentCount     = attendances.stream().filter(a -> a.getStatus() == EnumAttendStatus.ABSENT).count();
        long lateCount       = attendances.stream().filter(a -> a.getStatus() == EnumAttendStatus.LATE).count();
        long earlyLeaveCount = attendances.stream().filter(a -> a.getStatus() == EnumAttendStatus.EARLY_LEAVE).count();
        return Math.max(0, (int)(100 - absentCount * 5 - lateCount * 2 - earlyLeaveCount * 2));
    }

    // ── 점수 범위 검증 (0~100) ────────────────────────────────────────────────
    private void validateScore(Integer mid, Integer fin, Integer assignment) {
        if (isOutOfRange(mid) || isOutOfRange(fin) || isOutOfRange(assignment)) {
            throw new BusinessException(GradeErrorCode.SCORE_OUT_OF_RANGE);
        }
    }

    private boolean isOutOfRange(Integer score) {
        return score == null || score < 0 || score > 100;
    }
}