package com.green.member.application.member;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.common.kafka.StudentEvent;
import com.green.common.outbox.Outbox;
import com.green.common.outbox.OutboxRepository;
import com.green.member.application.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final StudentRepository studentRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

//
//    public void test(StudentCreateReq req) {
//
//        Student newStudent = new Student();
//        newStudent.setName( req.getName() );
//
//        studentRepository.save( newStudent );
//
//        StudentEvent studentEvent = StudentEvent.builder()
//                .memberCode(newStudent.getMemberCode() )
//                .name( newStudent.getName() )
//                .eventType( EventType.E_CREATED )
//                .build();
//
//        saveToOutbox(studentEvent);
//    }

    private void saveToOutbox(StudentEvent studentEvent) {
        try {
            String payload = objectMapper.writeValueAsString(studentEvent);
            Outbox outbox = Outbox.builder()
                    .topic("student-events")
                    .aggregateId( studentEvent.getMemberCode() )
                    .eventType( studentEvent.getEventType().name() )
                    .payload( payload )
                    .build();
            outboxRepository.save(outbox);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Outbox 직렬화 실패", e);
        }

    }
}
