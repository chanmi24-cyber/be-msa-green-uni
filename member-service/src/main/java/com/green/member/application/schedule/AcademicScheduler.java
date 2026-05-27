package com.green.member.application.schedule;

import com.green.common.enumcode.EnumScheduleType;
import com.green.member.entity.cache.ScheduleCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AcademicScheduler {

    private final SemesterSchedulerService semesterSchedulerService;
    private final ScheduleCacheRepository scheduleCacheRepository;

    /**
     * 매일 자정에 실행.
     * schedule_cache에서 오늘이 SEMESTER_START 시작일이면 학년/학기 자동 갱신.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void checkAndAdvanceSemester() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<ScheduleCache> todayStarts = scheduleCacheRepository
                .findByTypeAndIsActiveTrueAndStartDateBetween(
                        EnumScheduleType.SEMESTER_START, startOfDay, endOfDay);

        if (todayStarts.isEmpty()) {
            return;
        }

        log.info("[AcademicScheduler] 학기 시작 감지 ({}). 학년/학기 자동 갱신 시작", today);
        semesterSchedulerService.advanceSemester();
    }
}
