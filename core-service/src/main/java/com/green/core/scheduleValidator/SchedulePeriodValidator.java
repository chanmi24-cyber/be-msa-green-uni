package com.green.core.scheduleValidator;

import com.green.common.enumcode.EnumScheduleType;
import com.green.common.exception.BusinessException;
import com.green.common.exception.SchedulePeriodErrorCode;
import com.green.core.entity.cache.ScheduleCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.green.core.repository.ScheduleCacheRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SchedulePeriodValidator {

    private final ScheduleCacheRepository scheduleCacheRepository;

    // 수강신청, 수강정정 예외 로직
    public void checkCourseRegistrationOrModification() {
        boolean isRegistration = !scheduleCacheRepository
                .findByTypeAndIsActiveTrue(EnumScheduleType.COURSE_REGISTRATION)
                .isEmpty();

        boolean isModification = !scheduleCacheRepository
                .findByTypeAndIsActiveTrue(EnumScheduleType.COURSE_MODIFICATION)
                .isEmpty();

        if (!isRegistration && !isModification) {
            throw new BusinessException(SchedulePeriodErrorCode.NOT_COURSE_REGISTRATION_PERIOD);
        }
    }

    // 성적입력 기간 체크
    public void checkGradeInput() {
        boolean isActive = scheduleCacheRepository
                .findByTypeAndIsActiveTrue(EnumScheduleType.GRADE_INPUT)
                .isPresent();
        if (!isActive) throw new BusinessException(SchedulePeriodErrorCode.NOT_GRADE_INPUT_PERIOD);
    }

    // 성적조회 기간 체크
    public void checkGradeView() {
        boolean isActive = scheduleCacheRepository
                .findByTypeAndIsActiveTrue(EnumScheduleType.GRADE_VIEW)
                .isPresent();
        if (!isActive) throw new BusinessException(SchedulePeriodErrorCode.NOT_GRADE_VIEW_PERIOD);
    }

    // 성적이의신청 기간 체크
    public void checkGradeAppeal() {
        boolean isActive = scheduleCacheRepository
                .findByTypeAndIsActiveTrue(EnumScheduleType.GRADE_APPEAL)
                .isPresent();
        if (!isActive) throw new BusinessException(SchedulePeriodErrorCode.NOT_GRADE_APPEAL_PERIOD);
    }

    // 강의평가 기간 체크
    public void checkLectureEvaluation() {
        boolean isActive = scheduleCacheRepository
                .findByTypeAndIsActiveTrue(EnumScheduleType.LECTURE_EVALUATION)
                .isPresent();
        if (!isActive) throw new BusinessException(SchedulePeriodErrorCode.NOT_LECTURE_EVALUATION_PERIOD);
    }

    // 등록금납부 기간 체크
    public void checkTuitionPayment() {
        boolean isActive = scheduleCacheRepository
                .findByTypeAndIsActiveTrue(EnumScheduleType.TUITION_PAYMENT)
                .isPresent();
        if (!isActive) throw new BusinessException(SchedulePeriodErrorCode.NOT_TUITION_PAYMENT_PERIOD);
    }

    // 강의개설신청 기간 체크
    public void checkCourseOpen() {
        boolean isActive = scheduleCacheRepository
                .findByTypeAndIsActiveTrue(EnumScheduleType.LECTURE_REGISTRATION)
                .isPresent();
        if (!isActive) throw new BusinessException(SchedulePeriodErrorCode.NOT_LECTURE_REGISTRATION_PERIOD);
    }

    // 전과 신청 기간 체크
    public void checkMajorChange() {
        boolean isActive = scheduleCacheRepository
                .findByTypeAndIsActiveTrue(EnumScheduleType.MAJOR_CHANGE)
                .isPresent();
        if (!isActive) throw new BusinessException(SchedulePeriodErrorCode.NOT_MAJOR_CHANGE_PERIOD);
    }

    // 수강신청 또는 수강정정 기간 여부 확인 (예외 없이 boolean 반환)
    public boolean isCourseRegistrationOrModificationPeriod() {
        boolean isRegistration = scheduleCacheRepository
                .findByTypeAndIsActiveTrue(EnumScheduleType.COURSE_REGISTRATION)
                .isPresent();
        boolean isModification = scheduleCacheRepository
                .findByTypeAndIsActiveTrue(EnumScheduleType.COURSE_MODIFICATION)
                .isPresent();
        return isRegistration || isModification;
    }

    // 수강신청 기간 여부만 확인 (예외 없이 boolean 반환)
    public boolean isCourseRegistrationPeriod() {
        return scheduleCacheRepository
                .findByTypeAndIsActiveTrue(EnumScheduleType.COURSE_REGISTRATION)
                .isPresent();
    }

    // [추가] 강의평가 기간 활성화 여부 확인 (예외 없이 boolean 반환)
    public boolean isLectureEvaluationPeriod() {
        return scheduleCacheRepository
                .findByTypeAndIsActiveTrue(EnumScheduleType.LECTURE_EVALUATION)
                .isPresent();
    }

    // 수강정정 기간 시작일 반환 (활성 상태인 경우에만)
    public Optional<LocalDateTime> getCourseModificationStartDate() {
        return scheduleCacheRepository
                .findByTypeAndIsActiveTrue(EnumScheduleType.COURSE_MODIFICATION)
                .map(ScheduleCache::getStartDate);
    }
}