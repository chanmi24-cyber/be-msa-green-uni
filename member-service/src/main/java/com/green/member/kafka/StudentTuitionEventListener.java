package com.green.member.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.common.enumcode.EnumStudentStatus;
import com.green.common.kafka.TuitionPaidEvent;
import com.green.member.application.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentTuitionEventListener {

    private final StudentRepository studentRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "tuition-paid-topic", groupId = "member-service-group")
    @Transactional
    public void handleTuitionPaidEvent(ConsumerRecord<String, String> record) {
        try {
            TuitionPaidEvent event = objectMapper.readValue(record.value(), TuitionPaidEvent.class);

            int updatedRows = studentRepository.updateStatus(event.studentCode(), EnumStudentStatus.ENROLLED);

            if (updatedRows > 0) {
                log.info("[Member-Service] 학생 원본 DB 상태 변환 완료 -> ENROLLED (학생코드: {})", event.studentCode());
            } else {
                log.error("[Member-Service] 해당 학번의 학생을 찾을 수 없습니다. 코드: {}", event.studentCode());
            }
        } catch (Exception e) {
            log.error("[Member-Service] 이벤트 파싱 실패: {}", e.getMessage());
        }
    }
}