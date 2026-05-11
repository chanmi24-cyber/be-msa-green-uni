package com.green.core.application.course;

import com.green.common.enumcode.EnumApprovalStatus;
import com.green.common.enumcode.EnumScheduleType;
import com.green.core.application.course.model.*;
import com.green.core.application.lecture.repository.LectureRepository;
import com.green.core.application.lecture.repository.LectureScheduleRepository;
import com.green.core.entity.cache.StudentCache;
import com.green.core.entity.course.Course;
import com.green.core.entity.grade.Grade;
import com.green.core.entity.lecture.Lecture;
import com.green.core.entity.lecture.LectureSchedule;
import com.green.core.repository.GradeRepository;
import com.green.core.repository.ScheduleCacheRepository;
import com.green.core.repository.StudentCacheRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
                    return CourseRes.builder()
                            .lectureId(l.getLectureId())
                            .lectureName(l.getLectureName())
                            .credit(l.getCredit())
                            .lectureType(l.getLectureType().getValue())
                            .academicYear(l.getAcademicYear())
                            .maxStd(l.getMaxStd())
                            .remStd(l.getMaxStd() - enrolledCount)
                            .build();
                })
                .toList();
    }

    // API-ENRL-02: 내 수강 신청 목록 조회
    @Transactional(readOnly = true)
    public List<MyCourseRes> getMyCourses() {
        Long studentCode = Long.parseLong(request.getHeader("X-Member-Code"));
        int currentYear = LocalDate.now().getYear();
        int currentSemester = getCurrentSemester();

        return courseRepository
                .findByStudentCodeAndYearAndSemester(studentCode, currentYear, currentSemester)
                .stream()
                .map(c -> {
                    Lecture l = c.getLecture();
                    int enrolledCount = courseRepository.countByLecture_LectureIdAndYearAndSemester(
                            l.getLectureId(), currentYear, currentSemester);
                    return MyCourseRes.builder()
                            .lectureId(l.getLectureId())
                            .lectureName(l.getLectureName())
                            .credit(l.getCredit())
                            .lectureType(l.getLectureType().getValue())
                            .academicYear(l.getAcademicYear())
                            .maxStd(l.getMaxStd())
                            .remStd(l.getMaxStd() - enrolledCount)
                            .build();
                })
                .toList();
    }

    // API-ENRL-03: 수강 신청 실행
    @Transactional
    public CourseCreateRes createCourse(CourseCreateReq req) {
        int currentYear = LocalDate.now().getYear();
        int currentSemester = getCurrentSemester();

        Long studentCode = Long.parseLong(request.getHeader("X-Member-Code"));

        // 학생 정보 조회 (status 체크는 인터셉터에서 처리됨)
        StudentCache student = studentCacheRepository.findById(studentCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "학생 정보를 찾을 수 없습니다."));

        // 강의 조회 (APPROVED 상태만)
        Lecture lecture = lectureRepository.findById(req.getLectureId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 강의입니다."));
        if (lecture.getStatus() != EnumApprovalStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "수강 신청 가능한 강의가 아닙니다.");
        }
        log.info("3. 강의 조회 완료 - lectureId: {}", req.getLectureId());

        // 학과 조건 확인 (전공과목만 학과 제한, 교양은 무관)
        boolean isMajorSubject = lecture.getLectureType().name().startsWith("MAJOR");
        if (isMajorSubject) {
            Long lectureMajorId = lecture.getMajor().getMajorId();
            boolean isMajorMatch = lectureMajorId.equals(student.getMajorId())
                    || lectureMajorId.equals(student.getMinorId());
            if (!isMajorMatch) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "신청 대상 학과가 아닙니다.");
            }
        }

        // 학년 조건 확인
        if (!lecture.getAcademicYear().equals(student.getAcademicYear())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "신청 대상 학년이 아닙니다.");
        }
        log.info("4. 학과/학년 조건 확인 완료");

        // 이미 신청된 강의 확인
        boolean alreadyEnrolled = courseRepository
                .existsByStudentCodeAndLecture_LectureIdAndYearAndSemester(
                        studentCode, req.getLectureId(), currentYear, currentSemester);
        if (alreadyEnrolled) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 신청된 강의입니다.");
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
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        newSchedule.getDayOfWeek() + "요일 " + newSchedule.getStartPeriod()
                                + "교시 시간표가 중복됩니다.");
            }
        }
        log.info("6. 시간표 중복 확인 완료");

        // 수강 정원 초과 확인
        int enrolledCount = courseRepository.countByLecture_LectureIdAndYearAndSemester(
                req.getLectureId(), currentYear, currentSemester);
        if (enrolledCount >= lecture.getMaxStd()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "수강 정원이 초과되었습니다.");
        }
        log.info("7. 수강 정원 확인 완료 - 현재 {}/{}명", enrolledCount, lecture.getMaxStd());

        // 최대 학점(18학점) 초과 확인
        int currentCredits = courseRepository.sumCreditByStudentCodeAndYearAndSemester(
                studentCode, currentYear, currentSemester);
        int newCredits = lecture.getCredit();
        if (currentCredits + newCredits > 18) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "최대 신청 학점(18학점)을 초과합니다. 현재 신청 학점: " + currentCredits);
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
        int currentYear = LocalDate.now().getYear();
        int currentSemester = getCurrentSemester();

        Long studentCode = Long.parseLong(request.getHeader("X-Member-Code"));

        Course course = courseRepository
                .findByStudentCodeAndLecture_LectureIdAndYearAndSemester(
                        studentCode, lectureId, currentYear, currentSemester)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "수강 신청한 강의가 아닙니다."));

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