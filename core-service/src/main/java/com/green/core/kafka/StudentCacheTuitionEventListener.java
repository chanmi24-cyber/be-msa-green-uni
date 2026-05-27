package com.green.core.kafka;

import com.green.common.enumcode.EnumStudentStatus;
import com.green.common.kafka.TuitionPaidEvent;
import com.green.core.repository.StudentCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentCacheTuitionEventListener {

    private final StudentCacheRepository studentCacheRepository;

    @KafkaListener(topics = "tuition-paid-topic", groupId = "core-student-group")
    @Transactional // 💡 @Modifying 벌크 쿼리를 실행하므로 트랜잭션 선언이 필수입니다.
    public void handleTuitionPaidEvent(TuitionPaidEvent event) {
        log.info("[Core-Service] 등록금 납부 완료 이벤트 수신 -> Redis 캐시 갱신 시작 - 학생: {}", event.studentCode());

        // 1. findById 대신 정의되어 있는 findByMemberCode를 사용합니다.
        studentCacheRepository.findByMemberCode(event.studentCode()).ifPresentOrElse(
                cache -> {
                    // 2. 이미 구현되어 있는 updateStatus 쿼리 메서드를 호출하여 벌크 업데이트 처리합니다.
                    studentCacheRepository.updateStatus(event.studentCode(), EnumStudentStatus.ENROLLED);
                    log.info("[Core-Service] 학생 학사 캐시(DB) 상태 변환 완료 -> ENROLLED (학생코드: {})", event.studentCode());
                },
                () -> log.warn("[Core-Service] 캐시 저장소에 해당 학생의 캐시 정보가 존재하지 않습니다. 코드: {}", event.studentCode())
        );
    }
}