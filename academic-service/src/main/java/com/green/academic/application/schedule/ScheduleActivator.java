package com.green.academic.application.schedule;

import com.green.academic.entity.Schedule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleActivator {

    private final ScheduleRepository scheduleRepository;

//    // 매일 자정 실행
//    @Scheduled(cron = "0 0 0 * * *")
    //테스트용
    @Scheduled(fixedDelay = 5_000) // 5초마다
    @Transactional
    public void updateActiveStatus() {
        LocalDateTime now = LocalDateTime.now();
        List<Schedule> schedules = scheduleRepository.findAll();

        for (Schedule schedule : schedules) {
            boolean shouldBeActive = !now.isBefore(schedule.getStartDate())
                    && !now.isAfter(schedule.getEndDate());
            schedule.updateActive(shouldBeActive);
        }
        log.info("학사일정 활성화 상태 업데이트 완료");
    }
}