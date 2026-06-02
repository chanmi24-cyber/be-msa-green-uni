package com.green.core.application.course;

import com.green.common.enumcode.EnumApprovalStatus;
import com.green.common.enumcode.EnumBuilding;
import com.green.common.enumcode.EnumScheduleType;
import com.green.common.exception.BusinessException;
import com.green.core.application.course.model.*;
import com.green.core.application.lecture.repository.LectureExcludedMajorRepository;
import com.green.core.application.lecture.repository.LectureRepository;
import com.green.core.application.lecture.repository.LectureScheduleRepository;
import com.green.core.entity.cache.ProfessorCache;
import com.green.core.entity.cache.StudentCache;
import com.green.core.entity.course.Course;
import com.green.core.entity.lecture.Lecture;
import com.green.core.entity.lecture.LectureSchedule;
import com.green.core.exception.CourseErrorCode;
import com.green.core.repository.ProfessorCacheRepository;
import com.green.core.repository.ScheduleCacheRepository;
import com.green.core.repository.StudentCacheRepository;
import com.green.core.scheduleValidator.SchedulePeriodValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final LectureRepository lectureRepository;
    private final LectureScheduleRepository lectureScheduleRepository;
    private final ScheduleCacheRepository scheduleCacheRepository;
    private final StudentCacheRepository studentCacheRepository;
    private final ProfessorCacheRepository professorCacheRepository;
    private final SchedulePeriodValidator schedulePeriodValidator;
    private final LectureExcludedMajorRepository lectureExcludedMajorRepository;
    private final HttpServletRequest request;
    private final CourseTransactionService courseTransactionService; // 트랜잭션 분리 빈

    // ──────────────────────────────────────────────────────────────
    // API-ENRL-06: 수강 신청 페이지 활성화 제어
    // ──────────────────────────────────────────────────────────────
    /**
     * [수정] isOpen 반환 조건 버그 수정
     * - 기존: isEmpty() → isOpen(true)  ← 스케줄이 없을 때 열림 (반전 버그)
     * - 수정: isPresent() → isOpen(true) (활성 스케줄이 존재할 때만 열림)
     */
    @Transactional(readOnly = true)
    public CourseStatusRes getCourseStatus() {
        if (!scheduleCacheRepository.findByTypeAndIsActiveTrue(EnumScheduleType.COURSE_REGISTRATION).isEmpty()) {
            return CourseStatusRes.builder()
                    .isOpen(true)
                    .scheduleType(EnumScheduleType.COURSE_REGISTRATION)
                    .build();
        }
        if (!scheduleCacheRepository.findByTypeAndIsActiveTrue(EnumScheduleType.COURSE_MODIFICATION).isEmpty()) {
            return CourseStatusRes.builder()
                    .isOpen(true)
                    .scheduleType(EnumScheduleType.COURSE_MODIFICATION)
                    .build();
        }
        return CourseStatusRes.builder()
                .isOpen(false)
                .scheduleType(null)
                .build();
    }

    // ──────────────────────────────────────────────────────────────
    // API-ENRL-01: 수강 가능 강의 전체 조회
    // ──────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<CourseRes> getCourses(
            String lectureType,
            Long majorId,
            Integer academicYear,
            String search,
            Pageable pageable) {

        int currentYear = LocalDate.now().getYear();
        int currentSemester = getCurrentSemester();

        return lectureRepository
                .findByFilters(
                        currentYear, currentSemester, EnumApprovalStatus.APPROVED,
                        lectureType, majorId, academicYear, search, pageable)
                .map(l -> {
                    int enrolledCount = courseRepository.countByLecture_LectureIdAndYearAndSemesterAndIsDelFalse(
                            l.getLectureId(), currentYear, currentSemester);

                    List<LectureSchedule> schedules = lectureScheduleRepository
                            .findByLecture_LectureId(l.getLectureId());
                    String dayOfWeek = null;
                    Integer startPeriod = null;
                    Integer endPeriod = null;
                    EnumBuilding building = null;
                    String roomNumber = null;
                    if (!schedules.isEmpty()) {
                        LectureSchedule s = schedules.get(0);
                        dayOfWeek = s.getDayOfWeek();
                        startPeriod = s.getStartPeriod();
                        endPeriod = s.getEndPeriod();
                        building = s.getClassRoom().getBuilding();
                        roomNumber = s.getClassRoom().getRoom();
                    }

                    String proName = professorCacheRepository.findById(l.getMemberCode())
                            .map(ProfessorCache::getName)
                            .orElse(null);

                    return CourseRes.builder()
                            .lectureId(l.getLectureId())
                            .majorName(l.getMajor() != null ? l.getMajor().getName() : null)
                            .lectureName(l.getLectureName())
                            .building(building)
                            .roomNumber(roomNumber)
                            .lectureType(l.getLectureType().getValue())
                            .academicYear(l.getAcademicYear())
                            .proName(proName)
                            .dayOfWeek(dayOfWeek)
                            .startPeriod(startPeriod)
                            .endPeriod(endPeriod)
                            .credit(l.getCredit())
                            .maxStd(l.getMaxStd())
                            .remStd(l.getMaxStd() - enrolledCount)
                            .build();
                });
    }

    // ──────────────────────────────────────────────────────────────
    // API-ENRL-02: 내 수강 신청 목록 조회
    // ──────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public MyCourseListRes getMyCourses() {
        Long studentCode = Long.parseLong(request.getHeader("X-Member-Code"));
        int currentYear = LocalDate.now().getYear();
        int currentSemester = getCurrentSemester();

        List<MyCourseRes> courses = courseRepository
                .findByStudentCodeAndYearAndSemesterAndIsDelFalse(studentCode, currentYear, currentSemester)
                .stream()
                .map(c -> {
                    Lecture l = c.getLecture();
                    int enrolledCount = courseRepository.countByLecture_LectureIdAndYearAndSemesterAndIsDelFalse(
                            l.getLectureId(), currentYear, currentSemester);

                    List<LectureSchedule> schedules = lectureScheduleRepository
                            .findByLecture_LectureId(l.getLectureId());
                    String dayOfWeek = null;
                    Integer startPeriod = null;
                    Integer endPeriod = null;
                    EnumBuilding building = null;
                    String roomNumber = null;
                    if (!schedules.isEmpty()) {
                        LectureSchedule s = schedules.get(0);
                        dayOfWeek = s.getDayOfWeek();
                        startPeriod = s.getStartPeriod();
                        endPeriod = s.getEndPeriod();
                        building = s.getClassRoom().getBuilding();
                        roomNumber = s.getClassRoom().getRoom();
                    }

                    String proName = professorCacheRepository.findById(l.getMemberCode())
                            .map(ProfessorCache::getName)
                            .orElse(null);

                    return MyCourseRes.builder()
                            .lectureId(l.getLectureId())
                            .majorName(l.getMajor() != null ? l.getMajor().getName() : null)
                            .lectureName(l.getLectureName())
                            .building(building)
                            .roomNumber(roomNumber)
                            .lectureType(l.getLectureType().getValue())
                            .academicYear(l.getAcademicYear())
                            .proName(proName)
                            .dayOfWeek(dayOfWeek)
                            .startPeriod(startPeriod)
                            .endPeriod(endPeriod)
                            .credit(l.getCredit())
                            .maxStd(l.getMaxStd())
                            .remStd(l.getMaxStd() - enrolledCount)
                            .isAttended(0)
                            .build();
                })
                .toList();

        int totalCredits = courseRepository.sumCreditByStudentCodeAndYearAndSemester(
                studentCode, currentYear, currentSemester);

        return MyCourseListRes.builder()
                .totalEnrolledCredits(totalCredits)
                .courses(courses)
                .build();
    }

    // ──────────────────────────────────────────────────────────────
    // API-ENRL-03: 수강 신청 실행
    // ──────────────────────────────────────────────────────────────
    /**
     * 트랜잭션 없이 사전 검증만 수행한 뒤
     * CourseTransactionService.execute() 에 핵심 처리를 위임한다.
     *
     * ① 수강 기간 검증  ← 캐시 조회, DB 커넥션 불필요
     * ② 학생 정보 조회  ← 캐시 조회
     * ③ 강의 조회       ← 단순 PK 조회, 트랜잭션 컨텍스트 불필요
     * ④ 전공/학년 검증  ← 순수 비즈니스 로직
     * ⑤~⑨ CourseTransactionService 에서 READ_COMMITTED 트랜잭션으로 처리
     */
    public CourseCreateRes createCourse(CourseCreateReq req) {

        // ① 수강 기간 검증
        schedulePeriodValidator.checkCourseRegistrationOrModification();

        int currentYear = LocalDate.now().getYear();
        int currentSemester = getCurrentSemester();
        Long studentCode = Long.parseLong(request.getHeader("X-Member-Code"));

        // ② 학생 정보 조회
        StudentCache student = studentCacheRepository.findById(studentCode)
                .orElseThrow(() -> new BusinessException(CourseErrorCode.STUDENT_NOT_FOUND));

        // ③ 강의 조회
        Lecture lecture = lectureRepository.findById(req.getLectureId())
                .orElseThrow(() -> new BusinessException(CourseErrorCode.LECTURE_NOT_FOUND));
        if (lecture.getStatus() != EnumApprovalStatus.APPROVED) {
            throw new BusinessException(CourseErrorCode.LECTURE_NOT_APPROVED);
        }
        log.info("강의 조회 완료 - lectureId: {}", req.getLectureId());

        // ④ 전공/학년 조건 검증
        validateMajorAndAcademicYear(req.getLectureId(), lecture, student, studentCode);
        log.info("학과/학년 조건 확인 완료");

        // 시간표 목록은 트랜잭션 진입 전에 미리 조회
        List<LectureSchedule> newSchedules = lectureScheduleRepository
                .findByLecture_LectureId(req.getLectureId());

        // ⑤~⑨ 별도 빈의 트랜잭션으로 위임 (프록시 정상 동작 보장)
        return courseTransactionService.execute(
                req, lecture, student, studentCode,
                currentYear, currentSemester, newSchedules);
    }

    // ──────────────────────────────────────────────────────────────
    // API-ENRL-04: 수강 신청 취소
    // ──────────────────────────────────────────────────────────────
    /**
     * Course softDelete 만 수행한다.
     * Grade 는 Course.isDel 로 함께 관리되므로 별도 처리 불필요.
     * (성적 조회는 항상 Course.isDel = false 를 JOIN 조건으로 사용)
     */
    @Transactional
    public void deleteCourse(Long lectureId) {

        schedulePeriodValidator.checkCourseRegistrationOrModification();

        int currentYear = LocalDate.now().getYear();
        int currentSemester = getCurrentSemester();
        Long studentCode = Long.parseLong(request.getHeader("X-Member-Code"));

        Course course = courseRepository
                .findByStudentCodeAndLecture_LectureIdAndYearAndSemesterAndIsDelFalse(
                        studentCode, lectureId, currentYear, currentSemester)
                .orElseThrow(() -> new BusinessException(CourseErrorCode.COURSE_NOT_FOUND));

        course.softDelete();
        log.info("수강 신청 취소 완료 - lectureId: {}, studentCode: {}", lectureId, studentCode);
    }

    // ──────────────────────────────────────────────────────────────
    // 내부 헬퍼
    // ──────────────────────────────────────────────────────────────

    private void validateMajorAndAcademicYear(
            Long lectureId, Lecture lecture, StudentCache student, Long studentCode) {

        log.info("학생 majorId: {}, minorId: {}, 강의 majorId: {}",
                student.getMajorId(), student.getMinorId(), lecture.getMajor().getMajorId());

        boolean isMajorSubject = lecture.getLectureType().name().startsWith("MAJOR");

        if (isMajorSubject) {
            Long lectureMajorId = lecture.getMajor().getMajorId();
            boolean isMyMajor = lectureMajorId.equals(student.getMajorId());
            boolean isMyMinor = studentCacheRepository
                    .countMinorByStudentCodeAndMajorId(studentCode, lectureMajorId) > 0;

            if (!isMyMajor && !isMyMinor) {
                throw new BusinessException(CourseErrorCode.MAJOR_NOT_MATCHED);
            }

            boolean isMinorSubject = isMyMinor && !isMyMajor;
            if (!isMinorSubject && student.getAcademicYear() < lecture.getAcademicYear()) {
                throw new BusinessException(CourseErrorCode.ACADEMIC_YEAR_NOT_MATCHED);
            }

        } else if (lecture.getLectureType().name().equals("GENERAL_ELECTIVE")) {
            boolean isExcluded = lectureExcludedMajorRepository
                    .existsByLectureIdAndMajorIdOrMinorId(
                            lectureId,
                            student.getMajorId(),
                            student.getMinorId()
                    );
            if (isExcluded) {
                throw new BusinessException(CourseErrorCode.LIBERAL_ARTS_MAJOR_RESTRICTED);
            }
        }
    }

    private int getCurrentSemester() {
        int month = LocalDate.now().getMonthValue();
        return (month >= 3 && month <= 8) ? 1 : 2;
    }

    // 임시 - lecture 담당자 res 완성 전까지 테스트용
    @Transactional(readOnly = true)
    public List<CourseLectureRes> getLectures() {
        return lectureRepository.findAll().stream()
                .map(l -> CourseLectureRes.builder()
                        .lectureId(l.getLectureId())
                        .lectureName(l.getLectureName())
                        .build())
                .toList();
    }
}