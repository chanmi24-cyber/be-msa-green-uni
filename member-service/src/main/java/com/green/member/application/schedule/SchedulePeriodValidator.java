package com.green.member.application.schedule;

import com.green.common.enumcode.EnumScheduleType;
import com.green.common.exception.BusinessException;
import com.green.common.exception.SchedulePeriodErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SchedulePeriodValidator {

    private final ScheduleCacheRepository scheduleCacheRepository;

    // 전공변경신청 기간 체크
    public void checkMajorChange() {
        boolean isActive = scheduleCacheRepository
                .findByTypeAndIsActiveTrue(EnumScheduleType.MAJOR_CHANGE)
                .isPresent();
        if (!isActive) throw new BusinessException(SchedulePeriodErrorCode.NOT_MAJOR_CHANGE_PERIOD);
    }
}