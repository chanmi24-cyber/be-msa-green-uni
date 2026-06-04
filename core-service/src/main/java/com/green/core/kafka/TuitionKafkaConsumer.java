package com.green.core.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.core.application.tuition.TuitionService;
import com.green.core.application.tuition.model.StudentMajorChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TuitionKafkaConsumer {

    private final TuitionService tuitionService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "student-major-changed-topic", groupId = "tuition-group")
    public void consumeMajorChangedEvent(String message) {
        log.info("[Kafka 소비] 전과 발생 이벤트 수신: {}", message);
        try {
            // 수신된 JSON 메시지를 객체로 역직렬화
            StudentMajorChangedEvent event = objectMapper.readValue(message, StudentMajorChangedEvent.class);

            // 등록금 서비스의 이벤트 처리 메서드 호출
            tuitionService.handleStudentMajorChangedEvent(event.getStudentCode());

        } catch (Exception e) {
            log.error("[Kafka 에러] 전과 이벤트 처리 중 오류 발생. 메시지: {}, 사유: {}", message, e.getMessage(), e);
            // 대학교 시스템 특성상 정합성이 중요하므로, 필요시 데드레터큐(DLQ) 처리를 고려할 수 있습니다.
        }
    }
}
