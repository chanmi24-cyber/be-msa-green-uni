package com.green.member.application.member;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.common.enumcode.EnumMajorType;
import com.green.common.enumcode.EnumStudentStatus;
import com.green.common.kafka.StudentEvent;
import com.green.common.outbox.Outbox;
import com.green.common.outbox.OutboxRepository;
import com.green.member.application.admin.AdminRepository;
import com.green.member.application.member.model.MemberCreateReq;
import com.green.member.application.member.model.MemberCreateRes;
import com.green.member.application.professor.ProfessorRepository;
import com.green.member.application.student.StudentMajorRepository;
import com.green.member.application.student.StudentRepository;
import com.green.member.application.student.model.StudentCreateReq;
import com.green.member.configuration.MyFileUtil;
import com.green.member.entity.member.Member;
import com.green.member.entity.student.Student;
import com.green.member.entity.student.StudentMajor;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final StudentRepository studentRepository;
    private final StudentMajorRepository studentMajorRepository;
    private final ProfessorRepository professorRepository;
    private final AdminRepository adminRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final MyFileUtil myFileUtil;

    // 공통 처리: member 저장 + memberCode 생성
    private Member createMember(MemberCreateReq req, MultipartFile pic, String roleNum) {
        // 파일 처리
        String savedPicFileName = pic == null ? null : myFileUtil.makeRandomFileName(pic);

        // 순번 조회
        int seq = memberRepository.countStudentByEntryYear(req.getEntryDate().getYear());

        // memberCode 생성
        String entryYear = String.valueOf(req.getEntryDate().getYear());
        Long memberCode = Long.parseLong(entryYear + roleNum + String.format("%03d", seq));

        // 생일 → 초기 비밀번호 (raw)
        String rawPassword = req.getBirth().replace("-", "");

        Member member = Member.builder()
                .memberCode(memberCode)
                .email(req.getEmail())
                .name(req.getName())
                .birth(req.getBirth())
                .tel(req.getTel())
                .emergencyTel(req.getEmergencyTel())
                .postcode(req.getPostcode())
                .address(req.getAddress())
                .detailAddress(req.getDetailAddress())
                .entryDate(req.getEntryDate())
                .exitDate(req.getExitDate())
                .pic(savedPicFileName)
                .build();

        Member saved = memberRepository.save(member);

        // 파일 저장
        if (pic != null) {
            String middlePath = "member/" + memberCode;
            myFileUtil.makeFolders(middlePath);
            String fullFilePath = String.format("%s/%s", middlePath, savedPicFileName);
            try {
                myFileUtil.transferTo(pic, fullFilePath);
            } catch (IOException e) {
                saved.setPic(null);
                log.error("파일 저장 실패: {}", e.getMessage());
            }
        }

        return saved;
    }

    @Transactional
    public MemberCreateRes createStudent(StudentCreateReq req, MultipartFile pic) {
        Member member = createMember(req, pic, "1");

        Student student = Student.builder()
                .member(member)
                .academicYear(req.getAcademicYear())
                .semester(req.getSemester())
                .isTransfer(req.getIsTransfer() != null ? req.getIsTransfer() : false)
                .isMultiChild(req.getIsMultiChild() != null ? req.getIsMultiChild() : false)
                .isVeteran(req.getIsVeteran() != null ? req.getIsVeteran() : false)
                .status(req.getStatus() != null ? req.getStatus() : EnumStudentStatus.UNREGISTERED)
                .build();

        Student savedStudent = studentRepository.save(student);

        StudentMajor studentMajor = StudentMajor.builder()
                .student(savedStudent)
                .majorId(req.getMajorId())
                .type(EnumMajorType.PRIMARY)
                .build();

        studentMajorRepository.save(studentMajor);
    }

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
