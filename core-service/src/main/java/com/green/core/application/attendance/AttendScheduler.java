package com.green.core.application.attendance;

import com.green.core.entity.attendance.AttendanceSession;
import com.green.core.entity.lecture.LectureSchedule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendScheduler {

    // 교시 종료 시각 (endPeriod → 해당 교시가 끝나는 시각)
    private static final Map<Integer, LocalTime> PERIOD_END_TIME = Map.of(
            1, LocalTime.of(10, 0),
            2, LocalTime.of(11, 0),
            3, LocalTime.of(12, 0),
            4, LocalTime.of(13, 0),
            5, LocalTime.of(14, 0),
            6, LocalTime.of(15, 0),
            7, LocalTime.of(16, 0),
            8, LocalTime.of(17, 0),
            9, LocalTime.of(18, 0)
    );

    // 수업 종료 후 10분 버퍼
    private static final int GRACE_MINUTES = 10;

    private static final Map<DayOfWeek, String> DAY_KOR = Map.of(
            DayOfWeek.MONDAY,    "월",
            DayOfWeek.TUESDAY,   "화",
            DayOfWeek.WEDNESDAY, "수",
            DayOfWeek.THURSDAY,  "목",
            DayOfWeek.FRIDAY,    "금"
    );

    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendLectureScheduleRepository attendLectureScheduleRepository;
    private final AttendService attendService;

    /**
     * 5분마다 실행 — 교시 종료 시각 + 10분이 지난 활성 세션을 자동 종료.
     * 예) 3교시(~12:00) 수업 → 12:10 이후 자동 종료
     */
    @Scheduled(fixedDelay = 300_000) // 5분 (ms)
    @Transactional
    public void autoEndExpiredSessions() {
        List<AttendanceSession> activeSessions = attendanceSessionRepository.findAllActiveWithLecture();
        if (activeSessions.isEmpty()) return;

        LocalDate today = LocalDate.now();
        LocalTime now   = LocalTime.now();
        String todayKor = DAY_KOR.get(today.getDayOfWeek());

        for (AttendanceSession session : activeSessions) {
            Long lectureId = session.getLecture().getLectureId();

            List<LectureSchedule> todaySchedules = todayKor == null
                    ? List.of()
                    : attendLectureScheduleRepository.findByLectureIdAndDayOfWeek(lectureId, todayKor);

            LocalTime deadline = resolveDeadline(session, todaySchedules);
            if (deadline == null) continue;

            if (now.isAfter(deadline)) {
                log.info("[AttendScheduler] 세션 자동 종료: sessionId={}, lectureId={}, deadline={}",
                        session.getAttendsessionId(), lectureId, deadline);
                attendService.processSessionEnd(session);
            }
        }
    }

    /**
     * 자동 종료 기준 시각 결정.
     * - 오늘 정규 스케줄이 있으면: endPeriod 종료 시각 + 10분
     * - 보강(오늘 스케줄 없음): originalDate의 요일 스케줄 endPeriod + 10분
     * - 스케줄 판단 불가: 세션 시작 후 5시간 (안전망)
     */
    /**
     * 자동 종료 기준 시각 결정.
     * 1. 오늘 정규 스케줄 endPeriod + 10분
     * 2. 보강: originalDate 요일 스케줄 endPeriod + 10분
     * 3. 위 두 경우 모두 실패 시 세션 시작 후 5시간 (공통 안전망)
     */
    private LocalTime resolveDeadline(AttendanceSession session, List<LectureSchedule> todaySchedules) {
        // 1. 오늘 정규 스케줄
        LocalTime t = maxEndTime(todaySchedules);
        if (t != null) return t;

        // 2. 보강: originalDate 요일의 스케줄로 수업 길이 파악
        if (session.getOriginalDate() != null) {
            String originalDayKor = DAY_KOR.get(session.getOriginalDate().getDayOfWeek());
            if (originalDayKor != null) {
                List<LectureSchedule> originalSchedules = attendLectureScheduleRepository
                        .findByLectureIdAndDayOfWeek(session.getLecture().getLectureId(), originalDayKor);
                t = maxEndTime(originalSchedules);
                if (t != null) return t;
            }
        }

        // 3. 공통 안전망: 정규·보강 무관하게 스케줄 판단 실패 시 시작 후 5시간
        log.warn("[AttendScheduler] 스케줄 정보 없음 — 시작 후 5시간 기준 적용: sessionId={}", session.getAttendsessionId());
        return session.getStartedAt().toLocalTime().plusHours(5);
    }

    private LocalTime maxEndTime(List<LectureSchedule> schedules) {
        if (schedules.isEmpty()) return null;
        int maxEndPeriod = schedules.stream()
                .mapToInt(LectureSchedule::getEndPeriod)
                .max()
                .orElse(0);
        LocalTime periodEnd = PERIOD_END_TIME.get(maxEndPeriod);
        return periodEnd == null ? null : periodEnd.plusMinutes(GRACE_MINUTES);
    }
}