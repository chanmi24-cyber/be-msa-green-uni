package com.green.member.application.admin;

import com.green.common.constants.EventType;
import com.green.common.constants.UpdateType;
import com.green.common.enumcode.*;
import com.green.common.exception.CommonErrorCode;
import com.green.common.exception.FileErrorCode;
import com.green.member.application.major.model.AdminMajorRequestDetailRes;
import com.green.member.application.major.model.AdminMajorRequestProcessReq;
import com.green.member.application.major.model.AdminMajorRequestDetailDto;
import com.green.member.application.major.model.AdminMajorRequestListRes;
import com.green.member.application.major.model.AdminStudentMajorHistoryRes;
import com.green.member.application.professor.model.ProfessorListRes;
import com.green.member.application.major.MajorRequestRepository;
import com.green.member.application.status.StatusRequestRepository;
import com.green.member.application.status.model.AdminStatusRequestDetailDto;
import com.green.member.application.status.model.AdminStatusRequestDetailRes;
import com.green.member.application.status.model.AdminStatusRequestListRes;
import com.green.member.application.status.model.AdminStatusRequestProcessReq;
import com.green.member.application.student.model.*;
import com.green.member.entity.student.*;
import com.green.member.enumcode.EnumStatusRequestType;
import com.green.member.exception.MemberErrorCode;
import com.green.member.exception.RequestErrorCode;
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
import com.green.common.file.FileService;
import com.green.member.entity.member.Admin;
import com.green.member.entity.member.AdminHistory;
import com.green.member.entity.member.Member;
import com.green.member.entity.professor.Professor;
import com.green.member.entity.professor.ProfessorHistory;
import com.green.member.enumcode.EnumAdminStatus;
import com.green.member.enumcode.EnumMajorRequestType;
import com.green.member.enumcode.EnumProfessorPosition;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.YearMonth;
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
    private final FileService fileService;
    private final MemberService memberService;
    private final OutboxService outboxService;
    private final MemberHistoryService memberHistoryService;
    private final AdminHistoryRepository adminHistoryRepository;
    private final ProfessorHistoryRepository professorHistoryRepository;
    private final StudentHistoryRepository studentHistoryRepository;
    private final MajorRequestRepository majorRequestRepository;
    private final StatusRequestRepository statusRequestRepository;

    // 재직 중인 관리자인지 검증
    private void checkEmployedAdmin(Long memberCode) {
        Admin admin = adminRepository.findById(memberCode)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.ADMIN_NOT_FOUND));
        if (admin.getStatus() != EnumAdminStatus.EMPLOYMENT) {
            throw new BusinessException(MemberErrorCode.ADMIN_NOT_EMPLOYED);
        }
    }

    // 학생 목록 조회
    @Transactional(readOnly = true)
    public Page<StudentListRes> getStudents(
            Long memberCode, String status, Integer academicYear,
            String collegeName, String majorName, String search, Pageable pageable) {
        checkEmployedAdmin(memberCode);
        return studentRepository.findStudentList(status, academicYear, collegeName, majorName, search, pageable);
    }
    // 교수 목록 조회
    @Transactional(readOnly = true)
    public Page<ProfessorListRes> getProfessors(
            Long memberCode, String status, String majorName, String position, String search, Pageable pageable) {
        checkEmployedAdmin(memberCode);
        return professorRepository.findProfessorList(status, majorName, position, search, pageable);
    }
    // 관리자 목록 조회
    @Transactional(readOnly = true)
    public Page<AdminListRes> getAdmins(Long memberCode, String status, String search, Pageable pageable) {
        checkEmployedAdmin(memberCode);
        return adminRepository.findAdminList(status, search, pageable);
    }

    // 대시보드: 학생/교수/관리자 계정 수 조회
    @Transactional(readOnly = true)
    public MemberCountRes getMemberCounts(Long memberCode) {
        checkEmployedAdmin(memberCode);
        return new MemberCountRes(
                studentRepository.count(),
                professorRepository.count(),
                adminRepository.count()
        );
    }

    // 회원 계정 등록. 공통 처리: member 저장 + memberCode 생성
    private Member createMember(MemberCreateReq req, MultipartFile pic, EnumMemberRole role) {
        // 입학 연도
        int entryYear = req.getEntryDate().getYear();

        // 배치 등록 시 이전 행의 INSERT가 반영된 상태에서 순번을 계산하도록 명시적 플러시
        memberRepository.flush();

        // 이미 등록된 이메일인지 검사
        if( memberRepository.existsByEmail(req.getEmail()) ){
            throw new BusinessException(MemberErrorCode.DUPLICATE_EMAIL);
        }

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

        // memberCode 확정 이후에 파일 검증·저장
        String savedPicFileName = fileService.save(pic, "member/" + memberCode, FileService.ALLOWED_IMAGE_EXTENSIONS);

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

    // 학생 계정 등록
    @Transactional
    public MemberCreateRes createStudent(StudentCreateReq req, MultipartFile pic, Long updaterCode) {
        checkEmployedAdmin(updaterCode);
        Member member = createMember(req, pic, EnumMemberRole.STUDENT);

        // 학생 테이블 저장
        Student newStudent = Student.builder()
                .member(member)
                .academicYear(req.getAcademicYear())
                .semester(req.getSemester())
                .isTransfer(req.getIsTransfer() != null ? req.getIsTransfer() : false)
                .isMultiChild(req.getIsMultiChild() != null ? req.getIsMultiChild() : false)
                .isVeteran(req.getIsVeteran() != null ? req.getIsVeteran() : false)
                .status(req.getStatus())
                .build();

        Student savedStudent = studentRepository.save(newStudent);

        // 학생 주전공 테이블 저장
        StudentMajor studentMajor = StudentMajor.builder()
                .student(savedStudent)
                .majorId(req.getMajorId())
                .type(EnumMajorType.PRIMARY)
                .build();

        StudentMajor savedStudentMajor = studentMajorRepository.save(studentMajor);

        // 입학 이력 저장 (신규입학 / 편입학 구분)
        String changeType = savedStudent.getIsTransfer() ? "편입학" : "신규 입학";
        studentHistoryRepository.save(StudentHistory.builder()
                .student(savedStudent)
                .changeType(changeType)
                .oldStatus(null)
                .newStatus(savedStudent.getStatus())
                .startDate(member.getEntryDate())
                .updaterCode(updaterCode)
                .build());

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

    // 교수 계정 등록
    @Transactional
    public MemberCreateRes createProfessor(ProfessorCreateReq req, MultipartFile pic, Long updaterCode) {
        checkEmployedAdmin(updaterCode);
        Member member = createMember(req, pic, EnumMemberRole.PROFESSOR);

        Professor newProfessor = Professor.builder()
                .member(member)
                .majorId(req.getMajorId())
                .degree(req.getDegree())
                .position(req.getPosition())
                .labBuilding(req.getLabBuilding())
                .labRoom(req.getLabRoom())
                .labTel(req.getLabTel())
                .status(req.getStatus())
                .build();

        Professor savedProfessor = professorRepository.save(newProfessor);

        // 신규임용 이력 저장
        professorHistoryRepository.save(ProfessorHistory.builder()
                .professor(savedProfessor)
                .changeType("신규 임용")
                .oldStatus(null)
                .newStatus(savedProfessor.getStatus())
                .newPosition(savedProfessor.getPosition())
                .startDate(member.getEntryDate())
                .updaterCode(updaterCode)
                .build());

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

    // 관리자 계정 등록
    @Transactional
    public MemberCreateRes createAdmin(AdminCreateReq req, MultipartFile pic, Long updaterCode) {
        checkEmployedAdmin(updaterCode);
        Member member = createMember(req, pic, EnumMemberRole.ADMIN);

        Admin newAdmin = Admin.builder()
                .member(member)
                .status(req.getStatus())
                .build();

        Admin savedAdmin = adminRepository.save(newAdmin);

        // 신규입사 이력 저장
        adminHistoryRepository.save(AdminHistory.builder()
                .admin(savedAdmin)
                .changeType("신규 입사")
                .oldStatus(null)
                .newStatus(savedAdmin.getStatus())
                .startDate(member.getEntryDate())
                .updaterCode(updaterCode)
                .build());

        return MemberCreateRes.builder()
                .memberCode(member.getMemberCode())
                .build();
    }

    public MemberProfileRes getMemberProfile(Long memberCode, Long requesterCode) {
        checkEmployedAdmin(requesterCode);
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
            default -> throw new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND);
        };
    }

    // 학생 계정 개인 정보 수정
    @Transactional
    public void updateStudent(Long memberCode, Long updaterCode, AdminStudentUpdateReq req, MultipartFile pic) {
        checkEmployedAdmin(updaterCode);
        // 공통 필드 업데이트
        Member member = memberRepository.findById(memberCode).orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
        String oldName = member.getName();
        LocalDate oldBirth = member.getBirth();
        member.updateCommonByAdmin( req.getName(), req.getBirth(), savePic(memberCode, pic, member.getPic()) );

        // 학생 필드 업데이트
        Student student = studentRepository.findById(memberCode).orElseThrow(() -> new BusinessException(MemberErrorCode.STUDENT_NOT_FOUND));
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
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MAJOR_NOT_FOUND));
        boolean majorChanged = false;
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
            majorChanged = true;
        }
        if (!before.isEmpty()) {
            memberHistoryService.save(memberCode, updaterCode, before);
        }

        // StudentEvent Outbox 저장
        boolean nameChanged = req.getName() != null && !req.getName().equals(oldName);
        boolean transferChanged = req.getIsTransfer() != null && !req.getIsTransfer().equals(oldIsTransfer);
        boolean multiChildChanged = req.getIsMultiChild() != null && !req.getIsMultiChild().equals(oldIsMultiChild);
        boolean veteranChanged = req.getIsVeteran() != null && !req.getIsVeteran().equals(oldIsVeteran);
        if (nameChanged || transferChanged || multiChildChanged || veteranChanged || majorChanged) {
            StudentEvent studentEvent = StudentEvent.builder()
                    .memberCode(member.getMemberCode())
                    .name(member.getName())
                    .majorId(currentMajorId)
                    .isTransfer(student.getIsTransfer())
                    .isMultiChild(student.getIsMultiChild())
                    .isVeteran(student.getIsVeteran())
                    .eventType(EventType.E_UPDATED)
                    .updateType(UpdateType.PROFILE)
                    .build();
            outboxService.saveToOutbox(MemberTopic.STUDENT, member.getMemberCode(), studentEvent);
        }
    }

    // 교수 계정 정보 수정
    @Transactional
    public void updateProfessor(Long memberCode, Long updaterCode, AdminProfessorUpdateReq req, MultipartFile pic) {
        checkEmployedAdmin(updaterCode);
        // 공통 필드 업데이트
        Member member = memberRepository.findById(memberCode).orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
        String oldName = member.getName();
        LocalDate oldBirth = member.getBirth();
        member.updateCommonByAdmin( req.getName(), req.getBirth(), savePic(memberCode, pic, member.getPic()) );

        // 교수 필드 업데이트
        Professor professor = professorRepository.findById(memberCode).orElseThrow(() -> new BusinessException(MemberErrorCode.PROFESSOR_NOT_FOUND));
        EnumProfessorDegree oldDegree = professor.getDegree();
        Long oldMajorId = professor.getMajorId();
        professor.updateByAdmin( req.getDegree(), req.getMajorId() );

        // MemberHistory 저장
        Map<String, Object> before = new LinkedHashMap<>();
        if (req.getName() != null && !req.getName().equals(oldName)) before.put("name", oldName);
        if (req.getBirth() != null && !req.getBirth().equals(oldBirth)) before.put("birth", oldBirth);
        if (req.getDegree() != null && !req.getDegree().equals(oldDegree)) before.put("degree", oldDegree);
        if (req.getMajorId() != null && !req.getMajorId().equals(oldMajorId)) before.put("majorId", oldMajorId);
        if (!before.isEmpty()) {
            memberHistoryService.save(memberCode, updaterCode, before);
        }

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
                    .updateType(UpdateType.PROFILE)
                    .build();
            outboxService.saveToOutbox(MemberTopic.PROFESSOR, member.getMemberCode(), professorEvent);
        }
    }

    // 관리자 계정 정보 수정
    @Transactional
    public void updateAdmin(Long memberCode, Long updaterCode, AdminMemberUpdateReq req, MultipartFile pic) {
        checkEmployedAdmin(updaterCode);
        Member member = memberRepository.findById(memberCode).orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        String oldName = member.getName();
        LocalDate oldBirth = member.getBirth();

        // 공통 필드 업데이트
        member.updateCommonByAdmin( req.getName(), req.getBirth(), savePic(memberCode, pic, member.getPic()) );

        // MemberHistory 저장을 위한 변경된 필드만 수집
        Map<String, Object> before = new LinkedHashMap<>();
        if (req.getName() != null && !req.getName().equals(oldName)) before.put("name", oldName);
        if (req.getBirth() != null && !req.getBirth().equals(oldBirth)) before.put("birth", oldBirth);

        if (!before.isEmpty()) {
            memberHistoryService.save(memberCode, updaterCode, before);
        }
    }

    @Transactional
    public void updateAdminStatus(Long memberCode, Long updaterCode, StatusUpdateAdminReq req){
        checkEmployedAdmin(updaterCode);
        Admin admin = adminRepository.findById(memberCode).orElseThrow(() -> new BusinessException(MemberErrorCode.ADMIN_NOT_FOUND));
        EnumAdminStatus oldStatus = admin.getStatus();
        EnumAdminStatus newStatus = req.getStatus();

        if (oldStatus == EnumAdminStatus.RETIREMENT) {
            throw new BusinessException(MemberErrorCode.ALREADY_RETIRED);
        }
        if (oldStatus == newStatus) {
            throw new BusinessException(MemberErrorCode.SAME_STATUS);
        }

        // 상태 변경
        admin.updateStatus(req.getStatus());

        // changeType 결정
        String changeType;
        if (newStatus == EnumAdminStatus.RETIREMENT) {
            changeType = "퇴사";
            // 퇴사의 경우 exitDate 자동 세팅
            Member member = memberRepository.findById(memberCode).orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
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
                .updaterCode(updaterCode)
                .build();
        adminHistoryRepository.save(history);

        // 퇴사면 isActive = false 처리 (Kafka)
        if (newStatus == EnumAdminStatus.RETIREMENT) {
            AuthMemberEvent authEvent = AuthMemberEvent.builder()
                    .memberCode(memberCode)
                    .isActive(false)
                    .eventType(EventType.E_UPDATED)
                    .updateType(UpdateType.DEACTIVATE)
                    .build();
            outboxService.saveToOutbox(MemberTopic.AUTH_MEMBER, memberCode, authEvent);
        }
    }

    @Transactional
    public void updateProfessorStatus(Long memberCode, Long updaterCode, StatusUpdateProfessorReq req){
        checkEmployedAdmin(updaterCode);
        Professor professor = professorRepository.findById(memberCode).orElseThrow(() -> new BusinessException(MemberErrorCode.PROFESSOR_NOT_FOUND));
        EnumProfessorStatus oldStatus = professor.getStatus();
        EnumProfessorStatus newStatus = req.getStatus();
        EnumProfessorPosition oldPosition = professor.getPosition();
        EnumProfessorPosition newPosition = req.getPosition();

        String changeType;

        if (req.getStatus() == null && req.getPosition() == null) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }
        if (oldStatus == EnumProfessorStatus.RETIREMENT) {
            throw new BusinessException(MemberErrorCode.ALREADY_DISMISSED);
        }
        if (req.getStatus() != null && req.getStatus() == oldStatus) {
            throw new BusinessException(MemberErrorCode.SAME_STATUS);
        }
        if (req.getPosition() != null && req.getPosition() == oldPosition) {
            throw new BusinessException(MemberErrorCode.SAME_STATUS);
        }

        // 상태 변경
        if(req.getStatus() != null){
            professor.updateStatus(req.getStatus());
            if (newStatus == EnumProfessorStatus.RETIREMENT) {
                changeType = "퇴임";
                // exitDate 자동 세팅
                Member member = memberRepository.findById(memberCode).orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
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
                    .updaterCode(updaterCode)
                    .build();
            professorHistoryRepository.save(history);

            // ProfessorEvent Outbox 저장
            ProfessorEvent professorEvent = ProfessorEvent.builder()
                    .memberCode(professor.getMemberCode())
                    .status(professor.getStatus().getCode())
                    .eventType(EventType.E_UPDATED)
                    .updateType(UpdateType.STATUS)
                    .build();
            outboxService.saveToOutbox(MemberTopic.PROFESSOR, professor.getMemberCode(), professorEvent);

            // 퇴임이면 로그인 불가 처리
            if (newStatus == EnumProfessorStatus.RETIREMENT) {
                AuthMemberEvent authEvent = AuthMemberEvent.builder()
                        .memberCode(memberCode)
                        .isActive(false)
                        .eventType(EventType.E_UPDATED)
                        .updateType(UpdateType.DEACTIVATE)
                        .build();
                outboxService.saveToOutbox(MemberTopic.AUTH_MEMBER, memberCode, authEvent);
            }
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
                    .updaterCode(updaterCode)
                    .build();
            professorHistoryRepository.save(history);
        }
    }

    @Transactional
    public void updateStudentStatus(Long memberCode, Long updaterCode, StatusUpdateStudentReq req){
        checkEmployedAdmin(updaterCode);
        Student student = studentRepository.findById(memberCode).orElseThrow(() -> new BusinessException(MemberErrorCode.STUDENT_NOT_FOUND));
        EnumStudentStatus oldStatus = student.getStatus();
        EnumStudentStatus newStatus = req.getStatus();

        if (oldStatus == EnumStudentStatus.EXPULSION || oldStatus == EnumStudentStatus.QUIT) {
            throw new BusinessException(MemberErrorCode.ALREADY_TERMINATED);
        }
        if (oldStatus == newStatus) {
            throw new BusinessException(MemberErrorCode.SAME_STATUS);
        }

        // 상태 변경
        student.updateStatus(req.getStatus());

        // 퇴학, 자퇴, 졸업의 경우 exitDate 자동 세팅
        if(newStatus == EnumStudentStatus.EXPULSION || newStatus == EnumStudentStatus.QUIT || newStatus == EnumStudentStatus.GRADUATION){
            Member member = memberRepository.findById(memberCode).orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
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
                .note(req.getReason())
                .returnYear(req.getReturnYear())
                .returnSemester(req.getReturnSemester())
                .updaterCode(updaterCode)
                .build();
        studentHistoryRepository.save(history);

        // StudentEvent Outbox 저장
            StudentEvent studentEvent = StudentEvent.builder()
                    .memberCode(student.getMemberCode())
                    .status(student.getStatus().getCode())
                    .eventType(EventType.E_UPDATED)
                    .updateType(UpdateType.STATUS)
                    .build();
            outboxService.saveToOutbox(MemberTopic.STUDENT, student.getMemberCode(), studentEvent);

        // 자퇴/퇴학이면 로그인 불가 처리
        if (newStatus == EnumStudentStatus.EXPULSION || newStatus == EnumStudentStatus.QUIT) {
            AuthMemberEvent authEvent = AuthMemberEvent.builder()
                    .memberCode(memberCode)
                    .isActive(false)
                    .eventType(EventType.E_UPDATED)
                    .updateType(UpdateType.DEACTIVATE)
                    .build();
            outboxService.saveToOutbox(MemberTopic.AUTH_MEMBER, memberCode, authEvent);
        }
    }

    private String savePic(Long memberCode, MultipartFile pic, String oldFileName) {
        if (pic == null) return null;
        // 기존 사진이 있으면 먼저 삭제
        if (oldFileName != null) {
            fileService.delete(String.format("member/%s/%s", memberCode, oldFileName));
        }
        // 이미지 검증 후 저장 (JPG, JPEG, PNG만 허용)
        return fileService.save(pic, "member/" + memberCode, FileService.ALLOWED_IMAGE_EXTENSIONS);
    }
    // 관리자 상태 변경 이력 조회
    @Transactional(readOnly = true)
    public List<AdminHistoryRes> getStatusHistory(Long memberCode, Long requesterCode){
        checkEmployedAdmin(requesterCode);
        if (!adminRepository.existsById(memberCode)) {
            throw new BusinessException(MemberErrorCode.ADMIN_NOT_FOUND);
        }
        List<AdminHistory> histories = adminHistoryRepository.findByAdmin_MemberCodeOrderByCreatedAtDesc(memberCode);
        return histories.stream()
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

    // 전공 변경 신청 목록 전체 조회 (status/search 필터 + 페이지네이션)
    @Transactional(readOnly = true)
    public Page<AdminMajorRequestListRes> getMajorRequests(
            Long memberCode, String status, String search, Pageable pageable) {
        checkEmployedAdmin(memberCode);
        return majorRequestRepository.findAllByFilter(status, search, pageable);
    }

    // 전공 변경 신청 상세 조회 (신청 당시 전공 정보는 MajorRequest에 스냅샷으로 저장된 값 사용)
    @Transactional(readOnly = true)
    public AdminMajorRequestDetailRes getMajorRequestDetail( Long requestId, Long memberCode ) {
        checkEmployedAdmin(memberCode);
        AdminMajorRequestDetailDto detail = majorRequestRepository.findDetailByRequestId(requestId)
                .orElseThrow(() -> new BusinessException(RequestErrorCode.NOT_MAJOR_REQUEST));

        return AdminMajorRequestDetailRes.builder()
                .requestId(detail.getRequestId())
                .memberCode(detail.getMemberCode())
                .studentName(detail.getStudentName())
                .phone(detail.getPhone())
                .email(detail.getEmail())
                .academicStatus(detail.getAcademicStatus())
                .targetMajorName(detail.getTargetMajorName())
                .type(detail.getType())
                .status(detail.getStatus())
                .gpa(detail.getGpa())
                .reason(detail.getReason())
                .file(detail.getFile())
                .originalFileName(detail.getOriginalFileName())
                .rejectReason(detail.getRejectReason())
                .updaterName(detail.getUpdaterName())
                .academicYear(detail.getAcademicYear())
                .semester(detail.getSemester())
                .currentMajorName(detail.getCurrentMajorName())
                .currentMinorName(detail.getCurrentMinorName())
                .createdAt(detail.getCreatedAt())
                .updatedAt(detail.getUpdatedAt())
                .build();
    }

    // 전공 변경 신청 처리
    @Transactional
    public void updateMajorRequest(Long requestId, AdminMajorRequestProcessReq req, Long updaterCode) {
        checkEmployedAdmin(updaterCode);
        // APPROVED, REJECTED 외 상태는 처리 불가
        if (req.getStatus() != EnumApprovalStatus.APPROVED && req.getStatus() != EnumApprovalStatus.REJECTED) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }
        MajorRequest request = majorRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(RequestErrorCode.NOT_MAJOR_REQUEST));
        // PENDING 상태만 처리 가능
        if (request.getStatus() != EnumApprovalStatus.PENDING) {
            throw new BusinessException(RequestErrorCode.NOT_PROCESSABLE);
        }
        if (req.getStatus() == EnumApprovalStatus.APPROVED) {
            request.approve(updaterCode);

            Long studentCode = request.getStudent().getMemberCode();
            Long targetMajorId = request.getTargetMajorId();

            if (request.getType() == EnumMajorRequestType.MINOR) {
                // 부전공: 기존 부전공이 있으면 비활성화, 없으면 무시 (처음 신청일 수 있음)
                studentMajorRepository
                        .findByStudent_MemberCodeAndTypeAndIsActiveTrue(studentCode, EnumMajorType.MINOR)
                        .ifPresent(m -> m.deactivate());
                studentMajorRepository.save(StudentMajor.builder()
                        .student(request.getStudent())
                        .majorId(targetMajorId)
                        .type(EnumMajorType.MINOR)
                        .build());

                // 부전공 승인: 부전공 변경 이벤트 발행
                outboxService.saveToOutbox(
                        MemberTopic.STUDENT,
                        studentCode,
                        StudentEvent.builder()
                                .memberCode(studentCode)
                                .minorId(targetMajorId)
                                .eventType(EventType.E_UPDATED)
                                .updateType(UpdateType.MAJOR_MINOR)
                                .build()
                );

            } else { // TRANSFER: 전과
                // 기존 주전공 비활성화 후 새 주전공으로 교체
                studentMajorRepository
                        .findByStudent_MemberCodeAndTypeAndIsActiveTrue(studentCode, EnumMajorType.PRIMARY)
                        .orElseThrow(() -> new BusinessException(MemberErrorCode.MAJOR_NOT_FOUND))
                        .deactivate();
                studentMajorRepository.save(StudentMajor.builder()
                        .student(request.getStudent())
                        .majorId(targetMajorId)
                        .type(EnumMajorType.PRIMARY)
                        .build());

                // 전과 승인: 주전공 변경 이벤트 발행
                outboxService.saveToOutbox(
                        MemberTopic.STUDENT,
                        studentCode,
                        StudentEvent.builder()
                                .memberCode(studentCode)
                                .majorId(targetMajorId)
                                .eventType(EventType.E_UPDATED)
                                .updateType(UpdateType.MAJOR_TRANSFER)
                                .build()
                );
            }
        } else { // REJECTED: 반려
            if (req.getRejectReason() == null || req.getRejectReason().isBlank()) {
                throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
            }
            request.reject(req.getRejectReason(), updaterCode);
        }
    }

    // 전공 변경 신청서 파일 다운로드 (관리자 — 소유권 제한 없음)
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> getMajorRequestFile( Long requestId, Long memberCode) {
        checkEmployedAdmin(memberCode);
        MajorRequest request = majorRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(RequestErrorCode.NOT_MAJOR_REQUEST));
        if (request.getFile() == null) {
            throw new BusinessException(FileErrorCode.FILE_NOT_FOUND);
        }
        Long studentCode = request.getStudent().getMemberCode();
        String filePath = String.format("request/major/%s/%s", studentCode, request.getFile());
        return fileService.buildDownloadResponse(filePath, request.getOriginalFileName());
    }

    // 특정 학생의 전공 변경 이력 조회 (관리자용)
    @Transactional(readOnly = true)
    public List<AdminStudentMajorHistoryRes> getStudentMajorHistory(Long studentCode, Long adminCode) {
        checkEmployedAdmin(adminCode);
        if (!studentRepository.existsById(studentCode)) {
            throw new BusinessException(MemberErrorCode.STUDENT_NOT_FOUND);
        }
        return majorRequestRepository.findMajorHistoryByStudentCodeForAdmin(studentCode);
    }

    // 학적 변경 신청 목록 전체 조회 (status/search 필터 + 페이지네이션)
    @Transactional(readOnly = true)
    public Page<AdminStatusRequestListRes> getStatusRequests(
            Long memberCode, String status, String search, Pageable pageable) {
        checkEmployedAdmin(memberCode);
        return statusRequestRepository.findAllByFilter(status, search, pageable);
    }
    // 학적 변경 신청 상세 조회
    @Transactional(readOnly = true)
    public AdminStatusRequestDetailRes getStatusRequestDetail(Long requestId, Long memberCode ) {
        checkEmployedAdmin(memberCode);
        AdminStatusRequestDetailDto detail = statusRequestRepository.findDetailByRequestId(requestId)
                .orElseThrow(() -> new BusinessException(RequestErrorCode.NOT_STATUS_REQUEST));

        return AdminStatusRequestDetailRes.builder()
                .requestId(detail.getRequestId())
                .memberCode(detail.getMemberCode())
                .studentName(detail.getStudentName())
                .phone(detail.getPhone())
                .email(detail.getEmail())
                .totalCredits(detail.getTotalCredits())
                .currentMajorName(detail.getCurrentMajorName())
                .currentMinorName(detail.getCurrentMinorName())
                .academicStatus(detail.getAcademicStatus())
                .type(detail.getType())
                .status(detail.getStatus())
                .reason(detail.getReason())
                .file(detail.getFile())
                .originalFileName(detail.getOriginalFileName())
                .rejectReason(detail.getRejectReason())
                .updaterName(detail.getUpdaterName())
                .academicYear(detail.getAcademicYear())
                .returnYear(detail.getReturnYear())
                .returnSemester(detail.getReturnSemester())
                .semester(detail.getSemester())
                .createdAt(detail.getCreatedAt())
                .updatedAt(detail.getUpdatedAt())
                .startDate(detail.getStartDate())
                .build();
    }
    // 학적 변경 신청서 파일 다운로드 (관리자 — 소유권 제한 없음)
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> getStatusRequestFile( Long requestId, Long memberCode) {
        checkEmployedAdmin(memberCode);
        StatusRequest request = statusRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(RequestErrorCode.NOT_STATUS_REQUEST));
        if (request.getFile() == null) {
            throw new BusinessException(FileErrorCode.FILE_NOT_FOUND);
        }
        Long studentCode = request.getStudent().getMemberCode();
        String filePath = String.format("request/status/%s/%s", studentCode, request.getFile());
        return fileService.buildDownloadResponse(filePath, request.getOriginalFileName());
    }
    // 학적 변경 신청 처리
    @Transactional
    public void updateStatusRequest(Long requestId, AdminStatusRequestProcessReq req, Long updaterCode) {
        checkEmployedAdmin(updaterCode);

        // APPROVED, REJECTED 외 상태는 처리 불가
        if (req.getStatus() != EnumApprovalStatus.APPROVED && req.getStatus() != EnumApprovalStatus.REJECTED) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }
        StatusRequest request = statusRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(RequestErrorCode.NOT_STATUS_REQUEST));
        // PENDING 상태만 처리 가능
        if (request.getStatus() != EnumApprovalStatus.PENDING) {
            throw new BusinessException(RequestErrorCode.NOT_PROCESSABLE);
        }
        if (req.getStatus() == EnumApprovalStatus.APPROVED) {
            request.approve(req.getNote(), updaterCode);

            Long studentCode = request.getStudent().getMemberCode();
            Student student = studentRepository.findById(studentCode).orElseThrow(() -> new BusinessException(MemberErrorCode.STUDENT_NOT_FOUND));
            EnumStudentStatus oldStatus = student.getStatus();

            // 신청 유형과 현재 학적 상태 정합성 검증
            // 신청 시점 이후 관리자 직접 변경 등으로 상태가 달라진 경우 방어
            if (request.getType() == EnumStatusRequestType.ABSENCE && oldStatus != EnumStudentStatus.ENROLLED) {
                throw new BusinessException(MemberErrorCode.INVALID_STATUS_FOR_REQUEST);
            }
            if (request.getType() == EnumStatusRequestType.RETURN && oldStatus != EnumStudentStatus.ABSENCE) {
                throw new BusinessException(MemberErrorCode.INVALID_STATUS_FOR_REQUEST);
            }
            if (request.getType() == EnumStatusRequestType.QUIT && oldStatus != EnumStudentStatus.ENROLLED) {
                throw new BusinessException(MemberErrorCode.INVALID_STATUS_FOR_REQUEST);
            }

            // 변화값
            EnumStudentStatus newStatus;
            String changeType;

            if (request.getType() == EnumStatusRequestType.ABSENCE) {
                // 휴학 승인: 상태값 휴학으로 변경 이벤트 발행
                newStatus = EnumStudentStatus.ABSENCE;
                changeType = "휴학";
            } else if (request.getType() == EnumStatusRequestType.RETURN) {
                // 복학 승인: 상태값 재학으로 변경 이벤트 발행
                newStatus = EnumStudentStatus.ENROLLED;
                changeType = "복학";
            } else { // QUIT: 자퇴
                // 자퇴 승인: 상태값 자퇴로 변경 이벤트 발행
                newStatus = EnumStudentStatus.QUIT;
                changeType = "자퇴";
            }

            // 휴학인 경우에만 복학 예정으로 종료일 계산
            // 1학기 복학 → 해당 연도 2월 말, 2학기 복학 → 해당 연도 8월 31일
            LocalDate endDate = null;
            if (request.getType() == EnumStatusRequestType.ABSENCE
                    && request.getReturnYear() != null && request.getReturnSemester() != null) {
                if (request.getReturnSemester() == 1) {
                    endDate = YearMonth.of(request.getReturnYear(), 2).atEndOfMonth();
                } else {
                    endDate = LocalDate.of(request.getReturnYear(), 8, 31);
                }
            }

            // StudentHistory 저장
            StudentHistory history = StudentHistory.builder()
                    .student(student)
                    .changeType(changeType)
                    .oldStatus(oldStatus)
                    .newStatus(newStatus)
                    .startDate(request.getStartDate())
                    .endDate(endDate)
                    .note(req.getNote())
                    .returnYear(request.getReturnYear())
                    .returnSemester(request.getReturnSemester())
                    .updaterCode(updaterCode)
                    .build();
            studentHistoryRepository.save(history);

            // 학생 상태 업데이트
            student.updateStatus(newStatus);

            // 자퇴 승인 시
            if (newStatus == EnumStudentStatus.QUIT) {
                // 자퇴 처리일 기록
                Member member = memberRepository.findById(studentCode)
                        .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
                member.setExitDate(LocalDate.now());

                // 로그인 불가 처리 처리
                AuthMemberEvent authEvent = AuthMemberEvent.builder()
                        .memberCode(student.getMemberCode())
                        .isActive(false)
                        .eventType(EventType.E_UPDATED)
                        .updateType(UpdateType.DEACTIVATE)
                        .build();
                outboxService.saveToOutbox(MemberTopic.AUTH_MEMBER, student.getMemberCode(), authEvent);
            }

            // StudentEvent Outbox 저장
            outboxService.saveToOutbox(
                    MemberTopic.STUDENT,
                    studentCode,
                    StudentEvent.builder()
                            .memberCode(studentCode)
                            .status(newStatus.getCode())
                            .eventType(EventType.E_UPDATED)
                            .updateType(UpdateType.STATUS)
                            .build()
            );
        } else { // REJECTED: 반려
            if (req.getRejectReason() == null || req.getRejectReason().isBlank()) {
                throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
            }
            request.reject(req.getRejectReason(), updaterCode);
        }
    }

}
