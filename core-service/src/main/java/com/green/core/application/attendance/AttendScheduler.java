package com.green.core.application.attendance;

import com.green.core.entity.attendance.AttendanceSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendScheduler {

    // [수정] 세션 시작 후 자동 종료까지 대기 시간 (교시 기준 → 시작 후 15분으로 변경)
    private static final int SESSION_EXPIRE_MINUTES = 15;

    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendService attendService;
    private final QrTokenRepository qrTokenRepository;

    /**
     * [수정] 1분마다 실행 — 세션 시작 후 15분이 지난 활성 세션을 자동 종료.
     * 교수가 직접 종료하거나 페이지를 이탈한 경우에도 15분 후 자동 정리됨.
     */
    @Scheduled(fixedDelay = 60_000) // 1분 (ms)
    public void autoEndExpiredSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(SESSION_EXPIRE_MINUTES);
        List<AttendanceSession> expiredSessions = attendanceSessionRepository.findAllActiveStartedBefore(cutoff);
        if (expiredSessions.isEmpty()) return;

        for (AttendanceSession session : expiredSessions) {
            log.info("[AttendScheduler] 세션 자동 종료 시도 (시작 후 {}분 경과): sessionId={}",
                    SESSION_EXPIRE_MINUTES, session.getAttendsessionId());
            try {
                attendService.processSessionEndById(session.getAttendsessionId());
            } catch (Exception e) {
                log.error("[AttendScheduler] 세션 자동 종료 실패 (다음 주기에 재시도): sessionId={}, 오류: {}",
                        session.getAttendsessionId(), e.getMessage());
            }
        }
    }

    // 매일 새벽 3시 만료 QR 토큰 일괄 삭제
    @Scheduled(cron = "0 0 3 * * *")
    public void deleteExpiredTokens() {
        int deleted = qrTokenRepository.deleteAllExpiredBefore(LocalDateTime.now());
        log.info("[AttendScheduler] 만료 QR 토큰 삭제 완료 - {}건", deleted);
    }
}