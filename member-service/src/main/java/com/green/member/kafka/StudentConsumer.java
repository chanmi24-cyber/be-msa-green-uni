package com.green.member.kafka;

import com.green.common.kafka.member.GpaResponseEvent;
import com.green.common.kafka.member.MemberTopic;
import com.green.member.application.major.MajorRequestRepository;
import com.green.member.entity.student.MajorRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentConsumer {
    private final MajorRequestRepository majorRequestRepository;

    @Transactional
    @KafkaListener(topics = MemberTopic.GPA_RESPONSE, groupId = "member-service-group")
    public void consumeGpaResponse(GpaResponseEvent event) {
        log.info("GPA 응답 수신 - requestId: {}, gpa: {}", event.getRequestId(), event.getGpa());
        try {
            MajorRequest request = majorRequestRepository.findById(event.getRequestId())
                    .orElse(null);
            if (request == null) {
                log.warn("GPA 응답 대상 신청 없음 - requestId: {}", event.getRequestId());
                return;
            }
            request.updateGpa(event.getGpa());
        } catch (Exception e) {
            log.error("GPA 업데이트 실패 - requestId: {}, error: {}", event.getRequestId(), e.getMessage());
        }
    }
}
