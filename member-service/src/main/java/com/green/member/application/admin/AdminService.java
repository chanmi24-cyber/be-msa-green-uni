package com.green.member.application.admin;

import com.green.common.constants.EventType;
import com.green.common.enumcode.*;
import com.green.common.exception.AuthErrorCode;
import com.green.common.exception.BusinessException;
import com.green.common.kafka.auth.AuthMemberEvent;
import com.green.common.kafka.member.ProfessorEvent;
import com.green.common.kafka.member.StudentEvent;
import com.green.common.kafka.member.MemberTopic;
import com.green.member.application.OutboxService;
import com.green.member.application.admin.model.*;
import com.green.member.application.member.MemberHistoryService;
import com.green.member.application.member.MemberRepository;
import com.green.member.application.member.MemberService;
import com.green.member.application.member.model.MemberCreateReq;
import com.green.member.application.member.model.MemberCreateRes;
import com.green.member.application.member.model.MemberProfileRes;
import com.green.member.application.professor.ProfessorHistoryRepository;
import com.green.member.application.professor.ProfessorRepository;
import com.green.member.application.professor.model.ProfessorCreateReq;
import com.green.member.application.professor.model.StatusUpdateProfessorReq;
import com.green.member.application.student.StudentHistoryRepository;
import com.green.member.application.student.StudentMajorRepository;
import com.green.member.application.student.StudentRepository;
import com.green.member.application.student.model.StatusUpdateStudentReq;
import com.green.member.application.student.model.StudentCreateReq;
import com.green.member.configuration.MyFileUtil;
import com.green.member.entity.member.Admin;
import com.green.member.entity.member.AdminHistory;
import com.green.member.entity.member.Member;
import com.green.member.entity.professor.Professor;
import com.green.member.entity.professor.ProfessorHistory;
import com.green.member.entity.student.Student;
import com.green.member.entity.student.StudentHistory;
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
import java.util.LinkedHashMap;
import java.util.List;
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
    private final MyFileUtil myFileUtil;
    private final MemberService memberService;
    private final OutboxService outboxService;
    private final MemberHistoryService memberHistoryService;
    private final AdminHistoryRepository adminHistoryRepository;
    private final ProfessorHistoryRepository professorHistoryRepository;
    private final StudentHistoryRepository studentHistoryRepository;

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
    @Transactional
    public void updateStudent(Long memberCode, Long updaterCode, AdminStudentUpdateReq req) {

        // 공통 필드 업데이트
        Member member = memberRepository.findById(memberCode).orElseThrow();
        String oldName = member.getName();
        LocalDate oldBirth = member.getBirth();
        member.updateCommonByAdmin( req.getName(),req.getBirth() );

        // 학생 필드 업데이트
        Student student = studentRepository.findById(memberCode).orElseThrow();
        Boolean oldIsTransfer = student.getIsTransfer();
        Boolean oldIsMultiChild = student.getIsMultiChild();
        Boolean oldIsVeteran = student.getIsVeteran();
        student.updateByAdmin(
                req.getIsTransfer(),
                req.getIsMultiChild(),
                req.getIsVeteran()
        );

        // MemberHistory 저장
        Map<String, Object> before = new LinkedHashMap<>();
        if (req.getName() != null && !req.getName().equals(oldName)) before.put("name", oldName);
        if (req.getBirth() != null && !req.getBirth().equals(oldBirth)) before.put("birth", oldBirth);
        if (req.getIsTransfer() != null && !req.getIsTransfer().equals(oldIsTransfer)) before.put("isTransfer", oldIsTransfer);
        if (req.getIsMultiChild() != null && !req.getIsMultiChild().equals(oldIsMultiChild)) before.put("isMultiChild", oldIsMultiChild);
        if (req.getIsVeteran() != null && !req.getIsVeteran().equals(oldIsVeteran)) before.put("isVeteran", oldIsVeteran);

        // 전공 변경 처리
        StudentMajor currentPrimaryMajor = studentMajorRepository
                .findByStudent_MemberCodeAndTypeAndIsActiveTrue(memberCode, EnumMajorType.PRIMARY)
                .orElseThrow();
        Long currentMajorId = currentPrimaryMajor.getMajorId();
        if (req.getMajorId() != null && !req.getMajorId().equals(currentMajorId)) {
            currentPrimaryMajor.deactivate();
            StudentMajor newPrimaryMajor = StudentMajor.builder()
                    .student(student)
                    .majorId(req.getMajorId())
                    .type(EnumMajorType.PRIMARY)
                    .build();
            studentMajorRepository.save(newPrimaryMajor);
            before.put("majorId", currentMajorId);
            currentMajorId = req.getMajorId();
        }
        memberHistoryService.save(memberCode, updaterCode, before);

        // StudentEvent Outbox 저장
        boolean nameChanged = req.getName() != null && !req.getName().equals(oldName);
        boolean transferChanged = req.getIsTransfer() != null && !req.getIsTransfer().equals(oldIsTransfer);
        boolean multiChildChanged = req.getIsMultiChild() != null && !req.getIsMultiChild().equals(oldIsMultiChild);
        boolean veteranChanged = req.getIsVeteran() != null && !req.getIsVeteran().equals(oldIsVeteran);
        if (nameChanged || transferChanged || multiChildChanged || veteranChanged) {
            StudentEvent studentEvent = StudentEvent.builder()
                    .memberCode(member.getMemberCode())
                    .name(member.getName())
                    .majorId(currentMajorId)  // 새 majorId
                    .isTransfer(student.getIsTransfer())
                    .isMultiChild(student.getIsMultiChild())
                    .isVeteran(student.getIsVeteran())
                    .eventType(EventType.E_UPDATED)
                    .updateType("PROFILE")
                    .build();
            outboxService.saveToOutbox(MemberTopic.STUDENT, member.getMemberCode(), studentEvent);
        }
    }

    // 교수 계정 정보 수정
    @Transactional
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
    @Transactional
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

    @Transactional
    public void updateAdminStatus(Long memberCode, Long updaterCode, StatusUpdateAdminReq req){
        Admin admin = adminRepository.findById(memberCode).orElseThrow();
        EnumAdminStatus oldStatus = admin.getStatus();
        EnumAdminStatus newStatus = req.getStatus();

        // 상태 변경
        admin.updateStatus(req.getStatus());

        // changeType 결정
        String changeType;
        if (newStatus == EnumAdminStatus.RETIREMENT) {
            changeType = "퇴사";
            // 퇴사의 경우 exitDate 자동 세팅
            Member member = memberRepository.findById(memberCode).orElseThrow();
            member.setExitDate(LocalDate.now());
        } else if (oldStatus == EnumAdminStatus.EMPLOYMENT && newStatus == EnumAdminStatus.ABSENCE) {
            changeType = "휴직";
        } else if (oldStatus == EnumAdminStatus.ABSENCE && newStatus == EnumAdminStatus.EMPLOYMENT) {
            changeType = "복직";
        } else {
            changeType = "상태변경";
        }

        // AdminHistory 저장
        AdminHistory history = AdminHistory.builder()
                .admin(admin)
                .changeType(changeType)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .reason(req.getReason())
                .updatorCode(updaterCode)
                .build();
        adminHistoryRepository.save(history);

        // 퇴사면 isActive = false 처리 (Kafka)
        if (newStatus == EnumAdminStatus.RETIREMENT) {
            AuthMemberEvent authEvent = AuthMemberEvent.builder()
                    .memberCode(memberCode)
                    .isActive(false)
                    .eventType(EventType.E_UPDATED)
                    .updateType("DEACTIVATE")
                    .build();
            outboxService.saveToOutbox(MemberTopic.AUTH_MEMBER, memberCode, authEvent);
        }
    }

    @Transactional
    public void updateProfessorStatus(Long memberCode, Long updaterCode, StatusUpdateProfessorReq req){
        Professor professor = professorRepository.findById(memberCode).orElseThrow();
        EnumProfessorStatus oldStatus = professor.getStatus();
        EnumProfessorStatus newStatus = req.getStatus();
        EnumProfessorPosition oldPosition = professor.getPosition();
        EnumProfessorPosition newPosition = req.getPosition();

        String changeType;

        // 상태 변경
        if(req.getStatus() != null){
            professor.updateStatus(req.getStatus());
            if (newStatus == EnumProfessorStatus.RETIREMENT) {
                changeType = "퇴임";
                // exitDate 자동 세팅
                Member member = memberRepository.findById(memberCode).orElseThrow();
                member.setExitDate(LocalDate.now());
            } else if (oldStatus == EnumProfessorStatus.EMPLOYMENT && newStatus == EnumProfessorStatus.ABSENCE) {
                changeType = "휴직";
            } else if (oldStatus == EnumProfessorStatus.ABSENCE && newStatus == EnumProfessorStatus.EMPLOYMENT) {
                changeType = "복직";
            } else if (newStatus == EnumProfessorStatus.SABBATICAL) {
                changeType = "안식년";
            } else if (oldStatus == EnumProfessorStatus.SABBATICAL && newStatus == EnumProfessorStatus.EMPLOYMENT) {
                changeType = "안식년종료";
            } else {
                changeType = "상태변경";
            }

            // ProfessorHistory 저장
            ProfessorHistory history = ProfessorHistory.builder()
                    .professor(professor)
                    .changeType(changeType)
                    .oldStatus(oldStatus)
                    .newStatus(newStatus)
                    .startDate(req.getStartDate())
                    .endDate(req.getEndDate())
                    .reason(req.getReason())
                    .updatorCode(updaterCode)
                    .build();
            professorHistoryRepository.save(history);

            // ProfessorEvent Outbox 저장
            ProfessorEvent professorEvent = ProfessorEvent.builder()
                    .memberCode(professor.getMemberCode())
                    .status(professor.getStatus().getCode())
                    .eventType(EventType.E_UPDATED)
                    .updateType("STATUS")
                    .build();
            outboxService.saveToOutbox(MemberTopic.PROFESSOR, professor.getMemberCode(), professorEvent);
        }

        // 직위 변경
        if(req.getPosition() != null){
            professor.updatePosition(req.getPosition());
            // ProfessorHistory 저장
            ProfessorHistory history = ProfessorHistory.builder()
                    .professor(professor)
                    .changeType("직위변경")
                    .oldPosition(oldPosition)
                    .newPosition(newPosition)
                    .startDate(req.getStartDate())
                    .endDate(req.getEndDate())
                    .reason(req.getReason())
                    .updatorCode(updaterCode)
                    .build();
            professorHistoryRepository.save(history);
        }
    }

    @Transactional
    public void updateStudentStatus(Long memberCode, Long updaterCode, StatusUpdateStudentReq req){
        Student student = studentRepository.findById(memberCode).orElseThrow();
        EnumStudentStatus oldStatus = student.getStatus();
        EnumStudentStatus newStatus = req.getStatus();

        // 상태 변경
        student.updateStatus(req.getStatus());

        // 퇴학, 자최, 졸업의 경우 exitDate 자동 세팅
        if(newStatus == EnumStudentStatus.EXPULSION || newStatus == EnumStudentStatus.QUIT || newStatus == EnumStudentStatus.GRADUATION){
            Member member = memberRepository.findById(memberCode).orElseThrow();
            member.setExitDate(LocalDate.now());
        }

        // changeType 결정
        String changeType;
        if (newStatus == EnumStudentStatus.EXPULSION) {
            changeType = "퇴학";
        } else if (newStatus == EnumStudentStatus.ABSENCE) {
            changeType = "휴학";
        } else if (oldStatus == EnumStudentStatus.ABSENCE && newStatus == EnumStudentStatus.ENROLLED) {
            changeType = "복학";
        } else if (newStatus == EnumStudentStatus.QUIT) {
            changeType = "자퇴";
        } else if (newStatus == EnumStudentStatus.GRADUATION) {
            changeType = "졸업";
        } else {
            changeType = "상태변경";
        }

        // StudentHistory 저장
        StudentHistory history = StudentHistory.builder()
                .student(student)
                .changeType(changeType)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .reason(req.getReason())
                .updatorCode(updaterCode)
                .build();
        studentHistoryRepository.save(history);

        // StudentEvent Outbox 저장
            StudentEvent studentEvent = StudentEvent.builder()
                    .memberCode(student.getMemberCode())
                    .status(student.getStatus().getCode())
                    .eventType(EventType.E_UPDATED)
                    .updateType("STATUS")
                    .build();
            outboxService.saveToOutbox(MemberTopic.STUDENT, student.getMemberCode(), studentEvent);

        // 자퇴/퇴학이면 로그인 불가 처리
        if (newStatus == EnumStudentStatus.EXPULSION || newStatus == EnumStudentStatus.QUIT) {
            AuthMemberEvent authEvent = AuthMemberEvent.builder()
                    .memberCode(memberCode)
                    .isActive(false)
                    .eventType(EventType.E_UPDATED)
                    .updateType("DEACTIVATE")
                    .build();
            outboxService.saveToOutbox(MemberTopic.AUTH_MEMBER, memberCode, authEvent);
        }
    }

    // 관리자 상태 변경 이력 조회
    @Transactional(readOnly = true)
    public List<AdminHistoryRes> findStatusHistory(Long memberCode){
        return adminHistoryRepository.findByAdmin_MemberCode(memberCode)
                .stream()
                .map( h -> {
                    AdminHistoryRes res = new AdminHistoryRes();
                    res.setChangeType(h.getChangeType());
                    res.setOldStatus(h.getOldStatus());
                    res.setNewStatus(h.getNewStatus());
                    res.setStartDate(h.getStartDate());
                    res.setEndDate(h.getEndDate());
                    res.setReason(h.getReason());
                    res.setCreatedAt(h.getCreatedAt());
                    return res;
                })
                .toList()
                ;
    }
}
