package com.green.core.application.scholarship;

import com.green.common.enumcode.EnumScheduleType;
import com.green.core.repository.ScheduleCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScholarshipScheduler {

    private final ScholarshipService scholarshipService;
    private final ScheduleCacheRepository scheduleCacheRepository;

    @Scheduled(cron = "0 0 2 * * *")
    public void runScholarshipAssignment() {
        var list = scheduleCacheRepository.findByTypeAndIsActiveTrue(EnumScheduleType.TUITION_PAYMENT);
        if (list.isEmpty()) {
            log.info("[ScholarshipScheduler] 활성 학기 없음, 스킵");
            return;
        }
        var schedule = list.get(0);
        log.info("[ScholarshipScheduler] 실행 - year={}, semester={}",
                schedule.getYear(), schedule.getSemester());
        scholarshipService.assignScholarships(schedule.getYear(), schedule.getSemester());
    }
}