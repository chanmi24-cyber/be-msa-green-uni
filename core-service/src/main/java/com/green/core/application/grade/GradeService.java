package com.green.core.application.grade;

import com.green.common.auth.MemberContext;
import com.green.common.enumcode.EnumApprovalStatus;
import com.green.common.exception.BusinessException;
import com.green.core.application.grade.model.GradeLectureListRes;
import com.green.core.application.grade.model.GradeListRes;
import com.green.core.application.grade.model.GradeStudentDetailRes;
import com.green.core.application.grade.model.GradeStudentRes;
import com.green.core.application.grade.model.GradeUpdateReq;
import com.green.core.entity.attendance.Attendance;
import com.green.core.entity.cache.StudentCache;
import com.green.core.entity.grade.Grade;
import com.green.core.entity.major.Major;
import com.green.core.enumcode.EnumAttendStatus;
import com.green.core.enumcode.EnumGradeLetter;
import com.green.core.exception.GradeErrorCode;
import com.green.core.repository.StudentCacheRepository;
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
    private final GradeMajorRepository gradeMajorRepository;
    private final StudentCacheRepository studentCacheRepository;

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

    // ── API-GPA-05: 학생 본인 성적 조회 ──────────────────────────────────────
    @Transactional(readOnly = true)
    public GradeStudentRes getStudentGrades(Integer year, Integer semester) {
        Long studentCode = MemberContext.get().memberCode();

        List<Grade> grades;
        if (year != null && semester != null) {
            grades = gradeRepository.findByStudentCodeAndYearAndSemester(studentCode, year, semester);
        } else if (year != null) {
            grades = gradeRepository.findByStudentCodeAndYear(studentCode, year);
        } else {
            grades = gradeRepository.findByStudentCode(studentCode);
        }

        List<GradeStudentRes.GradeItem> gradeList = grades.stream().map(g -> {
            var course   = g.getCourse();
            var lecture  = course.getLecture();
            EnumGradeLetter letter = g.getGradeLetter();
            return new GradeStudentRes.GradeItem(
                    g.getCourseId(),
                    course.getYear(),
                    course.getSemester(),
                    lecture.getLectureName(),
                    lecture.getCredit(),
                    lecture.getLectureType().getValue(),
                    letter != null ? letter.getCode()   : null,
                    letter != null ? toGradePoint(letter) : null
            );
        }).toList();

        // summary: 성적이 입력된(lectureRating != null) 과목만 포함
        List<GradeStudentRes.GradeItem> graded = gradeList.stream()
                .filter(g -> g.getLectureRating() != null)
                .toList();

        double averageGpa    = 0.0;
        int    convertedScore = 0;
        int    totalCredits   = 0;

        if (!graded.isEmpty()) {
            double sumWeighted = graded.stream()
                    .mapToDouble(g -> g.getLectureRating() * g.getLectureCredit())
                    .sum();
            int sumCredits = graded.stream()
                    .mapToInt(GradeStudentRes.GradeItem::getLectureCredit)
                    .sum();
            averageGpa    = sumCredits > 0 ? Math.round(sumWeighted / sumCredits * 100.0) / 100.0 : 0.0;
            convertedScore = (int) Math.round(averageGpa / 4.5 * 100);
            totalCredits  = graded.stream()
                    .filter(g -> !"F".equals(g.getLectureGrade()))
                    .mapToInt(GradeStudentRes.GradeItem::getLectureCredit)
                    .sum();
        }

        return new GradeStudentRes(gradeList,
                new GradeStudentRes.Summary(averageGpa, convertedScore, totalCredits));
    }


    // ── API-GPA-06: 학생 전체 성적 상세조회 (GET /my/detail) ─────────────────
    @Transactional(readOnly = true)
    public GradeStudentDetailRes getStudentAllGrades() {
        Long studentCode = MemberContext.get().memberCode();

        List<Grade> grades = gradeRepository.findByStudentCode(studentCode);

        StudentCache studentCache = studentCacheRepository.findById(studentCode).orElse(null);
        GradeStudentDetailRes.StudentInfo studentInfo = buildStudentInfo(studentCache);

        List<GradeStudentDetailRes.GradeItem> gradeList = grades.stream().map(g -> {
            var course  = g.getCourse();
            var lecture = course.getLecture();
            EnumGradeLetter letter = g.getGradeLetter();
            Integer myRank     = letter != null
                    ? gradeRepository.countHigherScore(lecture.getLectureId(), g.getTotalScore()) + 1
                    : null;
            Integer totalCount = letter != null
                    ? gradeRepository.countByLectureId(lecture.getLectureId())
                    : null;
            return new GradeStudentDetailRes.GradeItem(
                    g.getCourseId(),
                    course.getYear(),
                    course.getSemester(),
                    lecture.getLectureName(),
                    lecture.getCredit(),
                    lecture.getLectureType().getValue(),
                    g.getMidScore(),
                    g.getFinScore(),
                    g.getAssignmentScore(),
                    g.getAttendScore(),
                    g.getTotalScore(),
                    letter != null ? letter.getCode()    : null,
                    letter != null ? toGradePoint(letter) : null,
                    myRank,
                    totalCount,
                    null
            );
        }).toList();

        List<GradeStudentDetailRes.GradeItem> graded = gradeList.stream()
                .filter(g -> g.getLectureRating() != null)
                .toList();

        double averageGpa    = 0.0;
        int    convertedScore = 0;
        int    totalCredits   = 0;
        double averageScore   = 0.0;
        String averageGrade   = null;

        if (!graded.isEmpty()) {
            double sumWeighted = graded.stream()
                    .mapToDouble(g -> g.getLectureRating() * g.getLectureCredit())
                    .sum();
            int sumCredits = graded.stream()
                    .mapToInt(GradeStudentDetailRes.GradeItem::getLectureCredit)
                    .sum();
            averageGpa    = sumCredits > 0 ? Math.round(sumWeighted / sumCredits * 100.0) / 100.0 : 0.0;
            convertedScore = (int) Math.round(averageGpa / 4.5 * 100);
            totalCredits  = graded.stream()
                    .filter(g -> !"F".equals(g.getLectureGrade()))
                    .mapToInt(GradeStudentDetailRes.GradeItem::getLectureCredit)
                    .sum();
            averageScore  = Math.round(
                    graded.stream().mapToInt(GradeStudentDetailRes.GradeItem::getTotalScore)
                            .average().orElse(0.0) * 10.0) / 10.0;
            averageGrade  = Grade.calcGradeLetter((int) averageScore).getCode();
        }

        int majorRank       = 1;
        int majorTotalCount = 1;
        if (studentCache != null) {
            Long   majorId     = studentCache.getMajorId();
            double sumWeighted = 0.0;
            int    sumCredits  = 0;
            for (Grade g : grades) {
                if (g.getGradeLetter() != null) {
                    int credit = g.getCourse().getLecture().getCredit();
                    sumWeighted += (double) g.getTotalScore() * credit;
                    sumCredits  += credit;
                }
            }
            double myGpa    = sumCredits > 0 ? sumWeighted / sumCredits : 0.0;
            majorTotalCount = gradeRepository.countMajorStudentsWithGrades(majorId);
            majorRank       = gradeRepository.countMajorStudentsWithHigherGpa(majorId, myGpa) + 1;
            if (majorTotalCount == 0) { majorTotalCount = 1; majorRank = 1; }
        }

        return new GradeStudentDetailRes(
                studentInfo,
                gradeList,
                new GradeStudentDetailRes.Summary(averageGpa, convertedScore, totalCredits,
                        averageScore, averageGrade, majorRank, majorTotalCount));
    }

    private GradeStudentDetailRes.StudentInfo buildStudentInfo(StudentCache studentCache) {
        if (studentCache == null) return null;
        Major major = gradeMajorRepository.findWithCollegeById(studentCache.getMajorId()).orElse(null);
        return new GradeStudentDetailRes.StudentInfo(
                studentCache.getName(),
                major != null ? major.getName()              : null,
                major != null ? major.getCollege().getName() : null,
                studentCache.getAcademicYear(),
                studentCache.getSemester()
        );
    }

    // 평점 변환: A+=4.5, A=4.0, B+=3.5, B=3.0, C+=2.5, C=2.0, D+=1.5, D=1.0, F=0.0
    private double toGradePoint(EnumGradeLetter letter) {
        return switch (letter) {
            case A_PLUS -> 4.5;
            case A      -> 4.0;
            case B_PLUS -> 3.5;
            case B      -> 3.0;
            case C_PLUS -> 2.5;
            case C      -> 2.0;
            case D_PLUS -> 1.5;
            case D      -> 1.0;
            case F      -> 0.0;
        };
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