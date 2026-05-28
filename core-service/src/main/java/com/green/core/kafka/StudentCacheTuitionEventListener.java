package com.green.core.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.common.enumcode.EnumStudentStatus;
import com.green.common.kafka.TuitionPaidEvent;
import com.green.core.repository.StudentCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentCacheTuitionEventListener {

    private final StudentCacheRepository studentCacheRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "tuition-paid-topic", groupId = "core-student-group")
    @Transactional
    public void handleTuitionPaidEvent(ConsumerRecord<String, String> record) {
        try {
            TuitionPaidEvent event = objectMapper.readValue(record.value(), TuitionPaidEvent.class);

            int updatedRows = studentCacheRepository.updateStatus(
                    event.studentCode(), EnumStudentStatus.ENROLLED);

            if (updatedRows > 0) {
                log.info("[Core] StudentCache 상태 → ENROLLED (학생코드: {})", event.studentCode());
            } else {
                log.warn("[Core] StudentCache 없음 - 학생코드: {}", event.studentCode());
            }
        } catch (Exception e) {
            log.error("[Core] 이벤트 파싱 실패: {}", e.getMessage());
        }
    }
}