package com.green.member.kafka;

import com.green.common.constants.EventType;
import com.green.common.constants.UpdateType;
import com.green.common.enumcode.EnumStudentStatus;
import com.green.common.kafka.member.GraduationCheckResponseEvent;
import com.green.common.kafka.member.MemberTopic;
import com.green.common.kafka.member.StudentEvent;
import com.green.member.application.OutboxService;
import com.green.member.application.member.MemberRepository;
import com.green.member.application.student.StudentHistoryRepository;
import com.green.member.application.student.StudentRepository;
import com.green.member.entity.member.Member;
import com.green.member.entity.student.Student;
import com.green.member.entity.student.StudentHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class GraduationResponseConsumer {
    private static final int GRADUATION_CREDIT_REQUIREMENT = 120;
    private static final long SYSTEM_UPDATER_CODE = 0L; // 자동 처리 시 시스템 코드

    private final StudentRepository studentRepository;
    private final MemberRepository memberRepository;
    private final StudentHistoryRepository studentHistoryRepository;
    private final OutboxService outboxService;

    @Transactional
    @KafkaListener(topics = MemberTopic.GRADUATION_RESPONSE, groupId = "member-service-group")
    public boolean consume(GraduationCheckResponseEvent event) {
        Long studentCode = event.getStudentCode();
        Integer totalCredits = event.getTotalCredits();

        log.info("[졸업 처리] studentCode={} 취득학점={}", studentCode, totalCredits);

        if (totalCredits < GRADUATION_CREDIT_REQUIREMENT) {
            log.info("[졸업 처리] studentCode={} 졸업 조건 미충족 ({}/{}학점)",
                    studentCode, totalCredits, GRADUATION_CREDIT_REQUIREMENT);
            return false;
        }

        Student student = studentRepository.findById(studentCode).orElse(null);
        if (student == null) {
            log.warn("[졸업 처리] studentCode={} 학생 없음 - 건너뜀", studentCode);
            return false;
        }
        // 재학 상태가 아니면 처리 안 함 (중복 처리 방어)
        if (student.getStatus() != EnumStudentStatus.ENROLLED) {
            log.info("[졸업 처리] studentCode={} 재학 상태 아님 ({}) - 건너뜀",
                    studentCode, student.getStatus());
            return false;
        }

        Member member = memberRepository.findById(studentCode).orElse(null);
        if (member == null) {
            log.warn("[졸업 처리] studentCode={} 회원 없음 - 건너뜀", studentCode);
            return false;
        }

        EnumStudentStatus oldStatus = student.getStatus();

        // 1. 학생 상태 → 졸업
        student.updateStatus(EnumStudentStatus.GRADUATION);

        // 2. 졸업일 세팅
        member.setExitDate(LocalDate.now());

        // 3. 이력 저장
        studentHistoryRepository.save(StudentHistory.builder()
                .student(student)
                .changeType("졸업")
                .oldStatus(oldStatus)
                .newStatus(EnumStudentStatus.GRADUATION)
                .startDate(LocalDate.now())
                .note("취득학점 " + totalCredits + "학점 - 자동 졸업 처리")
                .updaterCode(SYSTEM_UPDATER_CODE)
                .build());

        // 4. core-service StudentCache 상태 동기화
        outboxService.saveToOutbox(
                MemberTopic.STUDENT,
                studentCode,
                StudentEvent.builder()
                        .memberCode(studentCode)
                        .status(EnumStudentStatus.GRADUATION.getCode())
                        .eventType(EventType.E_UPDATED)
                        .updateType(UpdateType.STATUS)
                        .build()
        );

        log.info("[졸업 처리] 완료 - studentCode={} 취득학점={}학점", studentCode, totalCredits);
        return true;
    }
}
