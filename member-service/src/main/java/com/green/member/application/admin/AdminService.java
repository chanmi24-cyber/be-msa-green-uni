package com.green.member.application.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.common.constants.EventType;
import com.green.common.enumcode.*;
import com.green.common.exception.AuthErrorCode;
import com.green.common.exception.BusinessException;
import com.green.common.kafka.auth.AuthMemberEvent;
import com.green.common.kafka.member.ProfessorEvent;
import com.green.common.kafka.member.StudentEvent;
import com.green.common.kafka.member.MemberTopic;
import com.green.common.outbox.Outbox;
import com.green.common.outbox.OutboxRepository;
import com.green.member.application.OutboxService;
import com.green.member.application.admin.model.AdminCreateReq;
import com.green.member.application.admin.model.AdminMemberUpdateReq;
import com.green.member.application.admin.model.AdminProfessorUpdateReq;
import com.green.member.application.admin.model.AdminStudentUpdateReq;
import com.green.member.application.member.MemberHistoryRepository;
import com.green.member.application.member.MemberHistoryService;
import com.green.member.application.member.MemberRepository;
import com.green.member.application.member.MemberService;
import com.green.member.application.member.model.MemberCreateReq;
import com.green.member.application.member.model.MemberCreateRes;
import com.green.member.application.member.model.MemberProfileRes;
import com.green.member.application.member.model.MemberUpdateReq;
import com.green.member.application.professor.ProfessorRepository;
import com.green.member.application.professor.model.ProfessorCreateReq;
import com.green.member.application.student.StudentMajorRepository;
import com.green.member.application.student.StudentRepository;
import com.green.member.application.student.model.StudentCreateReq;
import com.green.member.configuration.MyFileUtil;
import com.green.member.entity.member.Admin;
import com.green.member.entity.member.Member;
import com.green.member.entity.member.MemberHistory;
import com.green.member.entity.professor.Professor;
import com.green.member.entity.student.Student;
import com.green.member.entity.student.StudentMajor;
import com.green.member.enumcode.EnumAdminStatus;
import com.green.member.enumcode.EnumProfessorPosition;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final MemberRepository memberRepository;
    private final StudentRepository studentRepository;
    private final StudentMajorRepository studentMajorRepository;
    private final ProfessorRepository professorRepository;
    private final AdminRepository adminRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final MyFileUtil myFileUtil;
    private final MemberService memberService;
    private final OutboxService outboxService;
    private final MemberHistoryService memberHistoryService;

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
                .role(role.getCode())
                .eventType(EventType.E_CREATED)
                .build();
        outboxService.saveToOutbox(MemberTopic.AUTH_MEMBER, member.getMemberCode(), authEvent);

        return newMember;
    }

    // 학생 정보 추가
    @Transactional
    public MemberCreateRes createStudent(StudentCreateReq req, MultipartFile pic) {
        Member member = createMember(req, pic, EnumMemberRole.STUDENT);

        // 학생 테이블 저장
        Student newStudent = Student.builder()
                .member(member)
                .academicYear(req.getAcademicYear())
                .semester(req.getSemester())
                .isTransfer(req.getIsTransfer() != null ? req.getIsTransfer() : false)
                .isMultiChild(req.getIsMultiChild() != null ? req.getIsMultiChild() : false)
                .isVeteran(req.getIsVeteran() != null ? req.getIsVeteran() : false)
                .status(req.getStatus() != null ? req.getStatus() : EnumStudentStatus.UNREGISTERED)
                .build();

        Student savedStudent = studentRepository.save(newStudent);

        // 학생 주전공 테이블 저장
        StudentMajor studentMajor = StudentMajor.builder()
                .student(savedStudent)
                .majorId(req.getMajorId())
                .type(EnumMajorType.PRIMARY)
                .build();

        StudentMajor savedStudentMajor = studentMajorRepository.save(studentMajor);

        // StudentEvent Outbox 저장
        StudentEvent studentEvent = StudentEvent.builder()
                .memberCode(member.getMemberCode())
                .name(member.getName())
                .email(member.getEmail())
                .academicYear(savedStudent.getAcademicYear())
                .semester(savedStudent.getSemester())
                .majorId(savedStudentMajor.getMajorId())
                .status(savedStudent.getStatus().getCode())
                .isTransfer(savedStudent.getIsTransfer())
                .isMultiChild(savedStudent.getIsMultiChild())
                .isVeteran(savedStudent.getIsVeteran())
                .eventType(EventType.E_CREATED)
                .build();
        outboxService.saveToOutbox(MemberTopic.STUDENT, member.getMemberCode(), studentEvent);

        return MemberCreateRes.builder()
                .memberCode(member.getMemberCode())
                .build();
    }

    // 교수 정보 추가
    @Transactional
    public MemberCreateRes createProfessor(ProfessorCreateReq req, MultipartFile pic) {
        Member member = createMember(req, pic, EnumMemberRole.PROFESSOR);

        Professor newProfessor = Professor.builder()
                .member(member)
                .majorId(req.getMajorId())
                .degree(req.getDegree())
                .position(req.getPosition() != null ? req.getPosition() : EnumProfessorPosition.PROFESSOR)
                .labBuilding(req.getLabBuilding())
                .labRoom(req.getLabRoom())
                .labTel(req.getLabTel())
                .status(req.getStatus() != null ? req.getStatus() : EnumProfessorStatus.EMPLOYMENT)
                .build();

        Professor savedProfessor = professorRepository.save(newProfessor);

        // ProfessorEvent Outbox 저장
        ProfessorEvent professorEvent = ProfessorEvent.builder()
                .memberCode(member.getMemberCode())
                .name(member.getName())
                .majorId(savedProfessor.getMajorId())
                .degree(savedProfessor.getDegree().getCode())
                .status(savedProfessor.getStatus().getCode())
                .eventType(EventType.E_CREATED)
                .build();

        outboxService.saveToOutbox(MemberTopic.PROFESSOR, member.getMemberCode(), professorEvent);

        return MemberCreateRes.builder()
                .memberCode(member.getMemberCode())
                .build();
    }

    // 관리자 정보 추가
    @Transactional
    public MemberCreateRes createAdmin(AdminCreateReq req, MultipartFile pic) {
        Member member = createMember(req, pic, EnumMemberRole.ADMIN);

        Admin newAdmin = Admin.builder()
                .member(member)
                .status(req.getStatus() != null ? req.getStatus() : EnumAdminStatus.EMPLOYMENT)
                .build();

        adminRepository.save(newAdmin);

        return MemberCreateRes.builder()
                .memberCode(member.getMemberCode())
                .build();
    }

    public MemberProfileRes getMemberProfile(Long memberCode) {
        EnumMemberRole role = getRoleFromMemberCode(memberCode);
        return memberService.getMyProfile(memberCode, role);
    }

    public EnumMemberRole getRoleFromMemberCode(Long memberCode) {
        String code = String.valueOf(memberCode);
        char roleChar = code.charAt(code.length() - 4);
        return switch (roleChar) {
            case '1' -> EnumMemberRole.STUDENT;
            case '2' -> EnumMemberRole.PROFESSOR;
            case '3' -> EnumMemberRole.ADMIN;
            default -> throw new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND);
        };
    }

    // 학생 계정 개인 정보 수정
    public void updateStudentProfile(Long memberCode, Long updatorCode, AdminStudentUpdateReq req) {
        Member member = memberRepository.findById(memberCode).orElseThrow();

        // 공통 필드 업데이트
        member.updateCommonByAdmin(
                req.getName(),
                req.getBirth()
        );

        // 학생 필드 업데이트
        Student student = studentRepository.findById(memberCode).orElseThrow();
        student.updateByAdmin(
                req.getIsTransfer(),
                req.getIsMultiChild(),
                req.getIsVeteran()
        );
    }

    // 교수 계정 정보 수정
    public void updateProfessor(Long memberCode, Long updaterCode, AdminProfessorUpdateReq req) {

        // 공통 필드 업데이트
        Member member = memberRepository.findById(memberCode).orElseThrow();
        String oldName = member.getName();
        LocalDate oldBirth = member.getBirth();
        member.updateCommonByAdmin( req.getName(),req.getBirth() );

        // 교수 필드 업데이트
        Professor professor = professorRepository.findById(memberCode).orElseThrow();
        EnumProfessorDegree oldDegree = professor.getDegree();
        Long oldMajorId = professor.getMajorId();
        professor.updateByAdmin( req.getDegree(), req.getMajorId() );

        // MemberHistory 저장
        Map<String, Object> before = new LinkedHashMap<>();
        if (req.getName() != null && !req.getName().equals(oldName)) before.put("name", oldName);
        if (req.getBirth() != null && !req.getBirth().equals(oldBirth)) before.put("birth", oldBirth);
        if (req.getDegree() != null && !req.getDegree().equals(oldDegree)) before.put("degree", oldDegree);
        if (req.getMajorId() != null && !req.getMajorId().equals(oldMajorId)) before.put("majorId", oldMajorId);
        memberHistoryService.save(memberCode, updaterCode, before);

        // ProfessorEvent Outbox 저장
        boolean nameChanged = req.getName() != null && !req.getName().equals(oldName);
        boolean degreeChanged = req.getDegree() != null && !req.getDegree().equals(oldDegree);
        boolean majorChanged = req.getMajorId() != null && !req.getMajorId().equals(oldMajorId);
        if (nameChanged || degreeChanged || majorChanged) {
            ProfessorEvent professorEvent = ProfessorEvent.builder()
                    .memberCode(member.getMemberCode())
                    .name(member.getName())
                    .degree(professor.getDegree().getCode())
                    .majorId(professor.getMajorId())
                    .eventType(EventType.E_UPDATED)
                    .updateType("PROFILE")
                    .build();
            outboxService.saveToOutbox(MemberTopic.PROFESSOR, member.getMemberCode(), professorEvent);
        }
    }

    // 관리자 계정 정보 수정
    public void updateAdmin(Long memberCode, Long updaterCode, AdminMemberUpdateReq req) {
        Member member = memberRepository.findById(memberCode).orElseThrow();

        String oldName = member.getName();
        LocalDate oldBirth = member.getBirth();

        // 공통 필드 업데이트
        member.updateCommonByAdmin(
                req.getName(),
                req.getBirth()
        );

        // MemberHistory 저장을 위한 변경된 필드만 수집
        Map<String, Object> before = new LinkedHashMap<>();
        if (req.getName() != null && !req.getName().equals(oldName)) before.put("name", oldName);
        if (req.getBirth() != null && !req.getBirth().equals(oldBirth)) before.put("birth", oldBirth);

        memberHistoryService.save(memberCode, updaterCode, before);
    }
}
