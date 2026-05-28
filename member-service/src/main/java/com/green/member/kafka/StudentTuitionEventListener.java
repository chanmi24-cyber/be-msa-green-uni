package com.green.member.kafka;

import com.green.common.enumcode.EnumStudentStatus;
import com.green.common.kafka.TuitionPaidEvent;
import com.green.member.application.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentTuitionEventListener {

    private final StudentRepository studentRepository;

    @KafkaListener(topics = "tuition-paid-topic", groupId = "member-student-group")
    @Transactional
    public void handleTuitionPaidEvent(TuitionPaidEvent event) {
        log.info("[Member-Service] 등록금 납부 완료 이벤트 수신 -> 원본 DB 상태 변경 시작 - 학생: {}", event.studentCode());

        // 🎯 SpEL이 적용된 리포지토리 메서드 호출
        int updatedRows = studentRepository.updateStatus(event.studentCode(), EnumStudentStatus.ENROLLED);

        if (updatedRows > 0) {
            log.info("[Member-Service] 학생 원본 DB 상태 변환 완료 -> ENROLLED (학생코드: {})", event.studentCode());
        } else {
            log.error("[🚨 Member-Service] 이벤트를 수신했으나 해당 학번의 학생을 찾을 수 없습니다. 코드: {}", event.studentCode());
        }
    }
}