package com.green.member.application.member;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.common.constants.EventType;
import com.green.common.enumcode.EnumMajorType;
import com.green.common.enumcode.EnumMemberRole;
import com.green.common.enumcode.EnumStudentStatus;
import com.green.common.kafka.AuthMemberEvent;
import com.green.common.kafka.KafkaTopic;
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

    // 회원 정보 추가. 공통 처리: member 저장 + memberCode 생성
    private Member createMember(MemberCreateReq req, MultipartFile pic, EnumMemberRole role) {
        // 파일 처리
        String savedPicFileName = pic == null ? null : myFileUtil.makeRandomFileName(pic);

        // 입학 연도
        int entryYear = req.getEntryDate().getYear();

        // 순번 조회
        int seq = switch (role) {
            case STUDENT   -> memberRepository.countStudentByEntryYear(entryYear);
            case PROFESSOR -> memberRepository.countProfessorByEntryYear(entryYear);
            case ADMIN     -> memberRepository.countAdminByEntryYear(entryYear);
        };

        String roleNum = switch (role) {
            case STUDENT   -> "1";
            case PROFESSOR -> "2";
            case ADMIN     -> "3";
        };

        // memberCode 생성
        Long memberCode = Long.parseLong(entryYear + roleNum + String.format("%03d", seq));

        // 생일 → 초기 비밀번호 (raw)
        String rawPassword = req.getBirth().toString().replace("-", "");

        // member table 저장
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

        Member newMember = memberRepository.save(member);

        // 파일 저장
        if (pic != null) {
            String middlePath = "member/" + memberCode;
            myFileUtil.makeFolders(middlePath);
            String fullFilePath = String.format("%s/%s", middlePath, savedPicFileName);
            try {
                myFileUtil.transferTo(pic, fullFilePath);
            } catch (IOException e) {
                newMember.setPic(null);
                log.error("파일 저장 실패: {}", e.getMessage());
            }
        }

        // AuthMemberEvent Outbox 저장
        AuthMemberEvent authEvent = AuthMemberEvent.builder()
                .memberCode(member.getMemberCode())
                .email(member.getEmail())
                .password(rawPassword)
                .role(role)
                .build();

        try {
            String payload = objectMapper.writeValueAsString(authEvent);
            Outbox outbox = Outbox.builder()
                    .topic(KafkaTopic.MEMBER)
                    .aggregateId(member.getMemberCode())
                    .eventType(EventType.E_CREATED.name())
                    .payload(payload)
                    .build();
            outboxRepository.save(outbox);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Outbox 직렬화 실패", e);
        }

        return newMember;
    }

    // 학생 정보 추가
    @Transactional
    public MemberCreateRes createStudent(StudentCreateReq req, MultipartFile pic) {
        Member member = createMember(req, pic, EnumMemberRole.STUDENT);

        // 학생 테이블 저장
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

        // 학생 주전공 테이블 저장
        StudentMajor studentMajor = StudentMajor.builder()
                .student(savedStudent)
                .majorId(req.getMajorId())
                .type(EnumMajorType.PRIMARY)
                .build();

        studentMajorRepository.save(studentMajor);

        return MemberCreateRes.builder()
                .memberCode(member.getMemberCode())
                .build();
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
