package com.green.core.scheduleValidator;

import com.green.common.enumcode.EnumScheduleType;
import com.green.common.exception.BusinessException;
import com.green.common.exception.SchedulePeriodErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.green.core.repository.ScheduleCacheRepository;

@Component
@RequiredArgsConstructor
public class SchedulePeriodValidator {

    private final ScheduleCacheRepository scheduleCacheRepository;

    // 수강신청 기간 체크
    public void checkCourseRegistration() {
        boolean isActive = scheduleCacheRepository
                .findByTypeAndIsActiveTrue(EnumScheduleType.COURSE_REGISTRATION)
                .isPresent();
        if (!isActive) throw new BusinessException(SchedulePeriodErrorCode.NOT_COURSE_REGISTRATION_PERIOD);
    }

    // 수강정정 기간 체크
    public void checkCourseModification() {
        boolean isActive = scheduleCacheRepository
                .findByTypeAndIsActiveTrue(EnumScheduleType.COURSE_MODIFICATION)
                .isPresent();
        if (!isActive) throw new BusinessException(SchedulePeriodErrorCode.NOT_COURSE_MODIFICATION_PERIOD);
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
                .findByTypeAndIsActiveTrue(EnumScheduleType.COURSE_OPEN)
                .isPresent();
        if (!isActive) throw new BusinessException(SchedulePeriodErrorCode.NOT_COURSE_OPEN_PERIOD);
    }

}