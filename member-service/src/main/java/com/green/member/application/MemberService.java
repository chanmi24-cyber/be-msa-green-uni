package com.green.member.application;

import com.green.common.constants.EventType;
import com.green.common.model.MemberEvent;
import com.green.member.application.model.MemberCreateReq;
import com.green.member.application.model.StudentCreateReq;
//import com.green.member.entity.Student;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
//    private final StudentRepository studentRepository;
//
//    public void createStudent(StudentCreateReq req) {
//        Student student = new Student();
//
//        student.setMemberCode(req.getMemberCode());
//        student.setMajorId(req.getMajorId());
//        student.setAcademicYear(req.getAcademicYear());
//        student.setSemester(req.getSemester());
//
//        // Null 체크 후 기본값 처리 또는 요청값 설정
//        if (req.getStatus() != null) {
//            student.setStatus(req.getStatus());
//        }
//
//        if (req.getIsTransfer() != null) {
//            student.setIsTransfer(req.getIsTransfer());
//        }
//
//        if (req.getIsMultiChild() != null) {
//            student.setIsMultiChild(req.getIsMultiChild());
//        }
//
//        if (req.getIsVeteran() != null) {
//            student.setIsVeteran(req.getIsVeteran());
//        }
//
//        studentRepository.save(student);
//    }
//
//    private void kafkaSend(MemberEvent memberEvent) {
//        kafkaTemplate.send("kafka-test", String.valueOf(memberEvent.getMemberCode()), memberEvent)
//                .whenComplete((result, ex) -> {
//                    if (ex == null) {
//                        // 성공 시 로그
//                        log.info("✅ [Kafka Success] Topic: {}, Offset: {}",
//                                result.getRecordMetadata().topic(),
//                                result.getRecordMetadata().offset());
//                    } else {
//                        // 실패 시 로그
//                        log.error("❌ [Kafka Failure] 원인: {}", ex.getMessage());
//                    }
//                });
//    }
}
