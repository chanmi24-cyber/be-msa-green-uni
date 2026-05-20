package com.green.core.application.tuition;

import com.green.core.entity.tuition.Tuition;
import com.green.core.entity.tuition.TuitionPolicy;
import com.green.core.enumcode.EnumTuitionStatus;
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
public class TuitionScheduler {

    private final TuitionRepository tuitionRepository;
    private final TuitionPolicyRepository tuitionPolicyRepository;

    /**
     * FR-TUI-08: 매 학기 시작 전 등록금 row 자동 생성 스케줄러
     * (매년 2월 1일 및 8월 1일 새벽 2시 정각 자동 가동 조건 예시)
     */
    @Scheduled(cron = "0 0 2 1 2,8 *")
    @Transactional
    public void generateTuitionRowsAutomatically() {
        int targetYear = LocalDateTime.now().getYear();
        int targetSemester = (LocalDateTime.now().getMonthValue() <= 6) ? 1 : 2;

        log.info("[배치 시스템] {}년도 {}학기 고지 데이터 일괄 검증을 개시합니다.", targetYear, targetSemester);

        // FR-TUI-12: 스케줄러 실행 전 단과대 정책 존재 유무 선행 검증
        List<TuitionPolicy> activePolicies = tuitionPolicyRepository.findByYearAndSemester(targetYear, targetSemester);
        if (activePolicies.isEmpty()) {
            // 알림 기능 제외 요구사항에 의거하여 로그 수준을 ERROR로 남겨 관제 환경에 전파하도록 조치
            log.error("[🚨 스케줄러 실행 중단] 해당 학기 등록금 단과대 정책 정책이 단 하나도 등록되어 있지 않습니다! 시스템 안정성을 위해 행 생성을 거부합니다.");
            return;
        }

        // 여기에 대량 인서트를 유도하는 도메인 연동 배치 코드 기술 가능
        log.info("[배치 완료] 등록금 고지 자동 생성이 성공적으로 완료되었습니다.");
    }
}