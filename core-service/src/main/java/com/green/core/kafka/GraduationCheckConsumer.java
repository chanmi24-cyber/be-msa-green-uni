package com.green.core.kafka;

import com.green.common.constants.EventType;
import com.green.common.kafka.member.GraduationCheckRequestEvent;
import com.green.common.kafka.member.GraduationCheckResponseEvent;
import com.green.common.kafka.member.MemberTopic;
import com.green.core.application.grade.GradeQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GraduationCheckConsumer {
    private final GradeQueryRepository gradeQueryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = MemberTopic.GRADUATION_REQUEST, groupId = "core-service-group")
    public void consume(GraduationCheckRequestEvent event) {
        Long studentCode = event.getStudentCode();
        log.info("[졸업 체크] studentCode={} 취득학점 계산 시작", studentCode);

        try {
            int totalCredits = gradeQueryRepository.sumTotalCreditsByStudentCode(studentCode);
            log.info("[졸업 체크] studentCode={} 취득학점={}", studentCode, totalCredits);

            GraduationCheckResponseEvent response = GraduationCheckResponseEvent.builder()
                    .studentCode(studentCode)
                    .totalCredits(totalCredits)
                    .eventType(EventType.E_CREATED)
                    .build();

            kafkaTemplate.send(MemberTopic.GRADUATION_RESPONSE,
                    String.valueOf(studentCode), response);
        } catch (Exception e) {
            log.error("[졸업 체크] studentCode={} 처리 중 오류 발생 - 건너뜀: {}", studentCode, e.getMessage(), e);
        }
    }
}
