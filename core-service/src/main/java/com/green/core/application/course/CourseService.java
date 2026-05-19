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
import com.green.core.entity.grade.Grade;
import com.green.core.entity.lecture.Lecture;
import com.green.core.entity.lecture.LectureSchedule;
import com.green.core.exception.CourseErrorCode;
import com.green.core.application.grade.GradeRepository; // [수정] grade 도메인 폴더로 이동
import com.green.core.repository.ProfessorCacheRepository;
import com.green.core.repository.ScheduleCacheRepository;
import com.green.core.repository.StudentCacheRepository;
import com.green.core.scheduleValidator.SchedulePeriodValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final GradeRepository gradeRepository;
    private final HttpServletRequest request;
    private final ProfessorCacheRepository professorCacheRepository;
    private final SchedulePeriodValidator schedulePeriodValidator;
    private final LectureExcludedMajorRepository lectureExcludedMajorRepository;

    // API-ENRL-06: 수강 신청 페이지 활성화 제어
    @Transactional(readOnly = true)
    public CourseStatusRes getCourseStatus() {
        if (scheduleCacheRepository.findByTypeAndIsActiveTrue(EnumScheduleType.COURSE_REGISTRATION).isPresent()) {
            return CourseStatusRes.builder()
                    .isOpen(true)
                    .scheduleType(EnumScheduleType.COURSE_REGISTRATION)
                    .build();
        }

        if (scheduleCacheRepository.findByTypeAndIsActiveTrue(EnumScheduleType.COURSE_MODIFICATION).isPresent()) {
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

    // API-ENRL-01: 수강 가능 강의 전체 조회
    @Transactional(readOnly = true)
    public List<CourseRes> getCourses() {
        int currentYear = LocalDate.now().getYear();
        int currentSemester = getCurrentSemester();

        return lectureRepository
                .findByYearAndSemesterAndStatusAndIsDelFalse(
                        currentYear, currentSemester, EnumApprovalStatus.APPROVED)
                .stream()
                .map(l -> {
                    int enrolledCount = courseRepository.countByLecture_LectureIdAndYearAndSemester(
                            l.getLectureId(), currentYear, currentSemester);

                    // 수업시간 및 강의실 정보
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

                    // 교수 이름
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
                })
                .toList();
    }

    // API-ENRL-02: 내 수강 신청 목록 조회
    @Transactional(readOnly = true)
    public MyCourseListRes getMyCourses() {
        Long studentCode = Long.parseLong(request.getHeader("X-Member-Code"));
        int currentYear = LocalDate.now().getYear();
        int currentSemester = getCurrentSemester();

        List<MyCourseRes> courses = courseRepository
                .findByStudentCodeAndYearAndSemester(studentCode, currentYear, currentSemester)
                .stream()
                .map(c -> {
                    Lecture l = c.getLecture();
                    int enrolledCount = courseRepository.countByLecture_LectureIdAndYearAndSemester(
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
                            .isAttended(0) // 수강신청 기간 중 항상 취소 가능
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

    // API-ENRL-03: 수강 신청 실행
    @Transactional
    public CourseCreateRes createCourse(CourseCreateReq req) {

        schedulePeriodValidator.checkCourseRegistrationOrModification();

        int currentYear = LocalDate.now().getYear();
        int currentSemester = getCurrentSemester();

        Long studentCode = Long.parseLong(request.getHeader("X-Member-Code"));

        // 학생 정보 조회 (status 체크는 인터셉터에서 처리됨)
        StudentCache student = studentCacheRepository.findById(studentCode)
                .orElseThrow(() -> new BusinessException(CourseErrorCode.STUDENT_NOT_FOUND));

        // 강의 조회 (APPROVED 상태만)
        Lecture lecture = lectureRepository.findById(req.getLectureId())
                .orElseThrow(() -> new BusinessException(CourseErrorCode.LECTURE_NOT_FOUND));
        if (lecture.getStatus() != EnumApprovalStatus.APPROVED) {
            throw new BusinessException(CourseErrorCode.LECTURE_NOT_APPROVED);
        }
        log.info("3. 강의 조회 완료 - lectureId: {}", req.getLectureId());

        log.info("학생 majorId: {}, minorId: {}, 강의 majorId: {}",
                student.getMajorId(), student.getMinorId(), lecture.getMajor().getMajorId());

        // 전공 여부 판단
        boolean isMajorSubject = lecture.getLectureType().name().startsWith("MAJOR");

        if (isMajorSubject) {
            // 전공 과목
            Long lectureMajorId = lecture.getMajor().getMajorId();

            boolean isMyMajor = lectureMajorId.equals(student.getMajorId());
            boolean isMyMinor = studentCacheRepository
                    .countMinorByStudentCodeAndMajorId(studentCode, lectureMajorId) > 0;

            if (!isMyMajor && !isMyMinor) {
                throw new BusinessException(CourseErrorCode.MAJOR_NOT_MATCHED);
            }

            // 학년 조건: 부전공이면 스킵, 아니면 학생 학년 >= 강의 학년
            boolean isMinorSubject = isMyMinor && !isMyMajor;
            if (!isMinorSubject) {
                if (student.getAcademicYear() < lecture.getAcademicYear()) {
                    // 상위 학년 강의는 수강 불가, 하위/동일 학년은 허용
                    throw new BusinessException(CourseErrorCode.ACADEMIC_YEAR_NOT_MATCHED);
                }
            }

        } else {
            // GENERAL_ELECTIVE : 키워드 매칭 학과 차단
            if (lecture.getLectureType().name().equals("GENERAL_ELECTIVE")) {
                boolean isExcluded = lectureExcludedMajorRepository
                        .existsByLectureIdAndMajorIdOrMinorId(
                                req.getLectureId(),
                                student.getMajorId(),
                                student.getMinorId()
                        );

                if (isExcluded) {
                    throw new BusinessException(CourseErrorCode.LIBERAL_ARTS_MAJOR_RESTRICTED);
                }
            }
        }

        log.info("4. 학과/학년 조건 확인 완료");

        // 이미 신청된 강의 확인
        boolean alreadyEnrolled = courseRepository
                .existsByStudentCodeAndLecture_LectureIdAndYearAndSemester(
                        studentCode, req.getLectureId(), currentYear, currentSemester);
        if (alreadyEnrolled) {
            throw new BusinessException(CourseErrorCode.COURSE_ALREADY_ENROLLED);
        }
        log.info("5. 중복 신청 확인 완료");

        // 시간표 중복 확인
        List<LectureSchedule> newSchedules = lectureScheduleRepository
                .findByLecture_LectureId(req.getLectureId());
        for (LectureSchedule newSchedule : newSchedules) {
            boolean timeConflict = courseRepository.existsTimeConflict(
                    studentCode, currentYear, currentSemester,
                    newSchedule.getDayOfWeek(),
                    newSchedule.getStartPeriod(),
                    newSchedule.getEndPeriod());
            if (timeConflict) {
                throw new BusinessException(CourseErrorCode.COURSE_SCHEDULE_CONFLICT);

            }
        }
        log.info("6. 시간표 중복 확인 완료");

        // 수강 정원 초과 확인
        int enrolledCount = courseRepository.countByLecture_LectureIdAndYearAndSemester(
                req.getLectureId(), currentYear, currentSemester);
        if (enrolledCount >= lecture.getMaxStd()) {
            throw new BusinessException(CourseErrorCode.COURSE_CAPACITY_EXCEEDED);
        }
        log.info("7. 수강 정원 확인 완료 - 현재 {}/{}명", enrolledCount, lecture.getMaxStd());

        // 최대 학점(18학점) 초과 확인
        int currentCredits = courseRepository.sumCreditByStudentCodeAndYearAndSemester(
                studentCode, currentYear, currentSemester);
        int newCredits = lecture.getCredit();
        if (currentCredits + newCredits > 18) {
            throw new BusinessException(CourseErrorCode.CREDIT_LIMIT_EXCEEDED);
        }
        log.info("8. 최대 학점 확인 완료 - 현재 {}학점 + 신규 {}학점", currentCredits, newCredits);

        // Course 저장
        Course course = Course.builder()
                .studentCode(studentCode)
                .lecture(lecture)
                .year(currentYear)
                .semester(currentSemester)
                .build();
        courseRepository.save(course);
        log.info("9. 수강 신청 저장 완료 - courseId: {}", course.getCourseId());

        // Grade Row 즉시 생성
        Grade grade = Grade.builder()
                .course(course)
                .build();
        gradeRepository.save(grade);
        log.info("10. 성적 Row 생성 완료 - courseId: {}", course.getCourseId());

        // 저장 후 총 학점 반환
        int totalCredits = currentCredits + newCredits;
        return CourseCreateRes.builder()
                .totalEnrolledCredits(totalCredits)
                .courses(List.of())
                .build();
    }

    // API-ENRL-04: 수강 신청 취소
    @Transactional
    public void deleteCourse(Long lectureId) {

        schedulePeriodValidator.checkCourseRegistrationOrModification();

        int currentYear = LocalDate.now().getYear();
        int currentSemester = getCurrentSemester();

        Long studentCode = Long.parseLong(request.getHeader("X-Member-Code"));

        Course course = courseRepository
                .findByStudentCodeAndLecture_LectureIdAndYearAndSemester(
                        studentCode, lectureId, currentYear, currentSemester)
                .orElseThrow(() -> new BusinessException(CourseErrorCode.COURSE_NOT_FOUND));

        // Course 삭제 시 Grade도 cascade로 함께 삭제됨 (Course 엔티티 @OneToOne cascade = ALL)
        courseRepository.delete(course);
        log.info("수강 신청 취소 완료 - lectureId: {}, studentCode: {}", lectureId, studentCode);
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