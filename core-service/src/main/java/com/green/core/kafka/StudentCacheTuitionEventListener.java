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
    @Transactional
    public void handleTuitionPaidEvent(TuitionPaidEvent event) {
        log.info("[Core-Service] 등록금 납부 완료 이벤트 수신 -> Redis 캐시 갱신 시작 - 학생: {}", event.studentCode());

        // 🎯 불필요한 조회를 생략하고, 벌크 연산을 직접 실행하여 성공한 row 수를 반환받습니다.
        int updatedRows = studentCacheRepository.updateStatus(event.studentCode(), EnumStudentStatus.ENROLLED);

        if (updatedRows > 0) {
            log.info("[Core-Service] 학생 학사 캐시(DB) 상태 변환 완료 -> ENROLLED (학생코드: {})", event.studentCode());
        } else {
            // 영향받은 row가 0개라는 것은 DB에 해당 memberCode를 가진 학생 캐시가 없다는 뜻입니다.
            log.warn("[Core-Service] 캐시 저장소에 해당 학생의 캐시 정보가 존재하지 않습니다. 코드: {}", event.studentCode());
        }
    }
}