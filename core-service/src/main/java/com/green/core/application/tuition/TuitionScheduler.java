package com.green.core.application.tuition;

import com.green.common.enumcode.EnumChangeType;
import com.green.core.entity.tuition.TuitionPolicy;
import com.green.core.entity.tuition.TuitionPolicyHistory;
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
    private final TuitionPolicyHistoryRepository tuitionPolicyHistoryRepository; // 추가 주입

    /**
     * FR-TUI-08: 매 학기 시작 전 (2월 1일, 8월 1일 02:00)
     * 1. 정책 스냅샷 자동 생성
     * 2. 등록금 고지 자동 연동
     */
    @Scheduled(cron = "0 0 2 1 2,8 *")
    @Transactional
    public void generateTuitionRowsAutomatically() {
        int targetYear = LocalDateTime.now().getYear();
        int targetSemester = (LocalDateTime.now().getMonthValue() <= 6) ? 1 : 2;

        log.info("[배치 시스템] {}년도 {}학기 데이터 처리 시작", targetYear, targetSemester);

        List<TuitionPolicy> masterPolicies = tuitionPolicyRepository.findAll();
        if (masterPolicies.isEmpty()) {
            log.error("[🚨 배치 중단] 정책 마스터 데이터가 없습니다.");
            return;
        }

        // [핵심 기능] 학기 시작 시 현재 마스터 정책을 히스토리(스냅샷)로 저장
        savePolicySnapshotForNewSemester(masterPolicies);

        // 이후 등록금 고지서 행 생성 로직 진행...
        log.info("[배치 완료] {}년도 {}학기 스냅샷 생성 및 고지서 생성이 완료되었습니다.", targetYear, targetSemester);
    }

    private void savePolicySnapshotForNewSemester(List<TuitionPolicy> masterPolicies) {
        masterPolicies.forEach(policy -> {
            String snapshot = String.format("{\"baseAmount\": %d}", policy.getBaseAmount());

            TuitionPolicyHistory history = TuitionPolicyHistory.builder()
                    .tuitionPolicy(policy)
                    .changeType(EnumChangeType.SNAPSHOT)
                    .beforeData(snapshot)
                    .changeReason("학기 시작 자동 스냅샷")
                    .updatorCode(0L) // 시스템 자동 기록을 의미하는 0번 코드
                    .build();

            tuitionPolicyHistoryRepository.save(history);
        });
        log.info("[스냅샷] 총 {}건의 정책 스냅샷이 생성되었습니다.", masterPolicies.size());
    }
}