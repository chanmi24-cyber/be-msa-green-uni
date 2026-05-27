package com.green.member.application.schedule;

import com.green.common.constants.EventType;
import com.green.common.constants.UpdateType;
import com.green.common.enumcode.EnumStudentStatus;
import com.green.common.kafka.member.GraduationCheckRequestEvent;
import com.green.common.kafka.member.MemberTopic;
import com.green.common.kafka.member.StudentEvent;
import com.green.member.application.OutboxService;
import com.green.member.application.student.StudentRepository;
import com.green.member.entity.student.Student;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SemesterSchedulerService {
    private final StudentRepository studentRepository;
    private final OutboxService outboxService;

    // 학기 시작 시 재학 중인 학생의 학년/학기를 자동 갱신
    @Transactional
    public void advanceSemester() {
        List<Student> enrolledStudents = studentRepository.findByStatus(EnumStudentStatus.ENROLLED);

        if (enrolledStudents.isEmpty()) {
            log.info("[학기 갱신] 재학 중인 학생 없음 - 갱신 건너뜀");
            return;
        }

        for (Student student : enrolledStudents) {
            Integer beforeYear = student.getAcademicYear();
            Integer beforeSemester = student.getSemester();

            student.advanceSemester();

            log.debug("[학기 갱신] memberCode={} | {}학년 {}학기 → {}학년 {}학기",
                    student.getMemberCode(), beforeYear, beforeSemester,
                    student.getAcademicYear(), student.getSemester());

            // core-service StudentCache 동기화
            outboxService.saveToOutbox(
                    MemberTopic.STUDENT,
                    student.getMemberCode(),
                    StudentEvent.builder()
                            .memberCode(student.getMemberCode())
                            .academicYear(student.getAcademicYear())
                            .semester(student.getSemester())
                            .eventType(EventType.E_UPDATED)
                            .updateType(UpdateType.SEMESTER_ADVANCE)
                            .build()
            );
        }
        log.info("[학기 갱신] 완료 - 처리 인원: {}명", enrolledStudents.size());
    }

    /**
     * 학기 종료 시 재학 중인 학생의 졸업 여부 확인 요청을 Kafka로 발행
     * core-service가 취득학점을 계산 후 GRADUATION_RESPONSE로 응답
     */
    @Transactional(readOnly = true)
    public void requestGraduationCheck() {
        List<Student> enrolledStudents = studentRepository.findByStatus(EnumStudentStatus.ENROLLED);

        if (enrolledStudents.isEmpty()) {
            log.info("[졸업 체크] 재학 중인 학생 없음 - 요청 건너뜀");
            return;
        }

        for (Student student : enrolledStudents) {
            outboxService.saveToOutbox(
                    MemberTopic.GRADUATION_REQUEST,
                    student.getMemberCode(),
                    GraduationCheckRequestEvent.builder()
                            .studentCode(student.getMemberCode())
                            .build()
            );
        }
        log.info("[졸업 체크] 요청 발행 완료 - 대상 인원: {}명", enrolledStudents.size());
    }
}
