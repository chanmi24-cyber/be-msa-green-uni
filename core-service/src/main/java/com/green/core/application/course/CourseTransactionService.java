package com.green.core.application.course;

import com.green.common.exception.BusinessException;
import com.green.core.application.course.model.CourseCreateReq;
import com.green.core.application.course.model.CourseCreateRes;
import com.green.core.application.grade.GradeRepository;
import com.green.core.entity.cache.StudentCache;
import com.green.core.entity.course.Course;
import com.green.core.entity.grade.Grade;
import com.green.core.entity.lecture.Lecture;
import com.green.core.entity.lecture.LectureSchedule;
import com.green.core.exception.CourseErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 수강 신청 핵심 처리 — 트랜잭션 분리 클래스
 *
 * CourseService.createCourse() 에서 트랜잭션 없이 사전 검증을 마친 뒤
 * 이 클래스의 execute() 를 호출한다.
 *
 * [분리 이유]
 * Spring @Transactional 은 프록시 기반으로 동작하므로
 * 같은 클래스 내 self-invocation 시 트랜잭션이 적용되지 않는다.
 * 별도 빈으로 분리해야 프록시가 정상 동작한다.
 *
 * [isolation = READ_COMMITTED]
 * 정원·중복 체크용 COUNT/EXISTS 가 매 쿼리마다 최신 커밋을 읽도록 설정.
 * MySQL InnoDB 기본값(REPEATABLE_READ) 에서는 같은 트랜잭션 내 스냅샷을
 * 바라보기 때문에 다른 트랜잭션이 커밋한 수강 신청을 못 볼 수 있다.
 *
 * [Grade 비활성화 미처리 이유]
 * Grade 는 Course 와 @MapsId 1:1 공유 PK 구조이며 별도 상태 필드가 없다.
 * 성적 조회는 항상 Course.isDel = false 를 JOIN 조건으로 사용하므로
 * 취소된 Course 의 Grade 는 자연스럽게 걸러진다.
 * Grade 에 isActive 등 상태 필드가 추가된다면 그때 담당자와 협의 후 반영한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseTransactionService {

    private final CourseRepository courseRepository;
    private final GradeRepository gradeRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CourseCreateRes execute(
            CourseCreateReq req,
            Lecture lecture,
            StudentCache student,
            Long studentCode,
            int currentYear,
            int currentSemester,
            List<LectureSchedule> newSchedules) {

        // ⑤ 중복 신청 확인
        //    DB unique constraint (student_code, lecture_id, year, semester) 병행 적용 권장.
        //    MySQL soft delete 환경에서는 partial unique index 대신
        //    비관적 락 + 이 exists 체크로 중복을 막는다.
        boolean alreadyEnrolled = courseRepository
                .existsByStudentCodeAndLecture_LectureIdAndYearAndSemesterAndIsDelFalse(
                        studentCode, req.getLectureId(), currentYear, currentSemester);
        if (alreadyEnrolled) {
            throw new BusinessException(CourseErrorCode.COURSE_ALREADY_ENROLLED);
        }
        log.info("중복 신청 확인 완료");

        // ⑥ 시간표 중복 확인
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
        log.info("시간표 중복 확인 완료");

        // ⑦ 정원 초과 확인 — 비관적 락(SELECT FOR UPDATE)으로 Race Condition 차단
        //    조회~저장 사이 다른 트랜잭션이 끼어들어 정원을 초과하는 것을 막는다.
        int enrolledCount = courseRepository
                .countByLecture_LectureIdAndYearAndSemesterAndIsDelFalseWithLock(
                        req.getLectureId(), currentYear, currentSemester);
        if (enrolledCount >= lecture.getMaxStd()) {
            throw new BusinessException(CourseErrorCode.COURSE_CAPACITY_EXCEEDED);
        }
        log.info("수강 정원 확인 완료 - 현재 {}/{}명", enrolledCount, lecture.getMaxStd());

        // ⑧ 최대 학점(18학점) 초과 확인
        int currentCredits = courseRepository.sumCreditByStudentCodeAndYearAndSemester(
                studentCode, currentYear, currentSemester);
        if (currentCredits + lecture.getCredit() > 18) {
            throw new BusinessException(CourseErrorCode.CREDIT_LIMIT_EXCEEDED);
        }
        log.info("최대 학점 확인 완료 - 현재 {}학점 + 신규 {}학점", currentCredits, lecture.getCredit());

        // ⑨ 수강 저장 또는 재활성화
        Course course = courseRepository
                .findByStudentCodeAndLecture_LectureIdAndYearAndSemesterAndIsDelTrue(
                        studentCode, req.getLectureId(), currentYear, currentSemester)
                .orElse(null);

        if (course != null) {
            // 수강정정 기간 재신청: Course 재활성화
            // Grade 는 Course.isDel 로 함께 관리되므로 별도 처리 불필요
            course.reactivate();
            log.info("수강 재활성화 완료 - courseId: {}", course.getCourseId());
        } else {
            // 신규 등록: Course + Grade 생성
            course = Course.builder()
                    .studentCode(studentCode)
                    .lecture(lecture)
                    .year(currentYear)
                    .semester(currentSemester)
                    .build();
            courseRepository.save(course);

            Grade grade = Grade.builder()
                    .course(course)
                    .build();
            gradeRepository.save(grade);
            log.info("수강 신청 및 성적 Row 생성 완료 - courseId: {}", course.getCourseId());
        }

        int totalCredits = currentCredits + lecture.getCredit();
        return CourseCreateRes.builder()
                .totalEnrolledCredits(totalCredits)
                .courses(List.of())
                .build();
    }
}