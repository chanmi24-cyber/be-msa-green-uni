package com.green.member.application.member;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.common.auth.MemberContext;
import com.green.common.constants.EventType;
import com.green.common.enumcode.EnumMemberRole;
import com.green.common.enumcode.EnumMajorType;
import com.green.common.kafka.auth.AuthMemberEvent;
import com.green.common.kafka.member.memberTopic;
import com.green.common.outbox.Outbox;
import com.green.common.outbox.OutboxRepository;
import com.green.member.application.admin.AdminRepository;
import com.green.member.application.admin.model.AdminProfileRes;
import com.green.member.application.member.model.MemberCreateReq;
import com.green.member.application.member.model.MemberProfileRes;
import com.green.member.application.member.model.MemberUpdateReq;
import com.green.member.application.professor.ProfessorRepository;
import com.green.member.application.professor.model.ProfessorProfileRes;
import com.green.member.application.student.StudentMajorRepository;
import com.green.member.application.student.StudentRepository;
import com.green.member.application.student.model.StudentProfileRes;
import com.green.member.configuration.MyFileUtil;
import com.green.member.entity.cache.MajorCache;
import com.green.member.entity.member.Admin;
import com.green.member.entity.member.Member;
import com.green.member.entity.professor.Professor;
import com.green.member.entity.student.Student;
import com.green.member.entity.student.StudentMajor;
import com.green.member.repository.MajorCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final MajorCacheRepository majorCacheRepository;
    private final StudentRepository studentRepository;
    private final StudentMajorRepository studentMajorRepository;
    private final ProfessorRepository professorRepository;
    private final AdminRepository adminRepository;
    private final MyFileUtil myFileUtil;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    // 내 정보 조회
    public MemberProfileRes getMyProfile(Long memberCode, EnumMemberRole role){
        MemberProfileRes memberProfile = switch (role) {
            case STUDENT   -> findStudent(memberCode, role);
            case PROFESSOR -> findProfessor(memberCode, role);
            case ADMIN     -> findAdmin(memberCode, role);
        };
        return memberProfile;
    }
    // 학생 정보 조회
    public StudentProfileRes findStudent(Long memberCode, EnumMemberRole role){
        Member memberInfo = memberRepository.findById(memberCode).orElseThrow();
        log.info("memberInfo : {}", memberInfo);
        Student studentInfo = studentRepository.findById(memberCode).orElseThrow();
        log.info("studentInfo: {}", studentInfo);
        List<StudentMajor> majors = studentMajorRepository.findByStudent_MemberCodeAndIsActiveTrue(memberCode);
        log.info("majors: {}", majors);

        StudentMajor mainMajor = majors.stream()
                .filter(m -> m.getType() == EnumMajorType.PRIMARY)
                .findFirst()
                .orElseThrow();
        StudentMajor subMajor = majors.stream()
                .filter(m -> m.getType() == EnumMajorType.MINOR)
                .findFirst()
                .orElse(null);
        MajorCache mainMajorCache = majorCacheRepository.findById(mainMajor.getMajorId()).orElseThrow();
        String subMajorName = null;
        if (subMajor != null) {
            MajorCache subMajorCache = majorCacheRepository.findById(subMajor.getMajorId()).orElseThrow();
            subMajorName = subMajorCache.getName();
        }

        return StudentProfileRes.builder()
                .memberCode(memberInfo.getMemberCode())
                .role(role.getCode())
                // 기본 정보
                .name(memberInfo.getName())
                .email(memberInfo.getEmail())
                .address(memberInfo.getAddress())
                .pic(memberInfo.getPic())
                .birth(memberInfo.getBirth())
                .tel(memberInfo.getTel())
                .emergencyTel(memberInfo.getEmergencyTel())
                .postcode(memberInfo.getPostcode())
                .detailAddress(memberInfo.getDetailAddress())
                .entryDate(memberInfo.getEntryDate())
                .exitDate(memberInfo.getExitDate())
                // 학사 정보
                .academicYear(studentInfo.getAcademicYear())
                .semester(studentInfo.getSemester())
                .mainMajorName(mainMajorCache.getName())
                .subMajorName(subMajorName)
                .collegeName(mainMajorCache.getCollegeName())
                .isMultiChild(studentInfo.getIsMultiChild())
                .isTransfer(studentInfo.getIsTransfer())
                .isVeteran(studentInfo.getIsVeteran())
                .status(studentInfo.getStatus().getCode())
                .build();
    }
    // 교수 정보 조회
    public ProfessorProfileRes findProfessor(Long memberCode, EnumMemberRole role){
        Member memberInfo = memberRepository.findById(memberCode).orElseThrow();
        Professor professorInfo = professorRepository.findById(memberCode).orElseThrow();
        log.info("professorInfo: {}", professorInfo);

        MajorCache majorCache = majorCacheRepository.findById(professorInfo.getMajorId()).orElseThrow();

        return ProfessorProfileRes.builder()
                .memberCode(memberInfo.getMemberCode())
                .role(role.getCode())
                // 기본 정보
                .name(memberInfo.getName())
                .email(memberInfo.getEmail())
                .address(memberInfo.getAddress())
                .pic(memberInfo.getPic())
                .birth(memberInfo.getBirth())
                .tel(memberInfo.getTel())
                .emergencyTel(memberInfo.getEmergencyTel())
                .postcode(memberInfo.getPostcode())
                .detailAddress(memberInfo.getDetailAddress())
                .entryDate(memberInfo.getEntryDate())
                .exitDate(memberInfo.getExitDate())
                // 학사 정보
                .degree(professorInfo.getDegree().getCode())
                .position(professorInfo.getPosition().getCode())
                .majorName(majorCache.getName())
                .collegeName(majorCache.getCollegeName())
                .labBuilding(professorInfo.getLabBuilding().getCode())
                .labRoom(professorInfo.getLabRoom())
                .labTel(professorInfo.getLabTel())
                .status(professorInfo.getStatus().getCode())
                .build();
    }
    // 관리자 정보 조회
    public AdminProfileRes findAdmin(Long memberCode, EnumMemberRole role){
        log.info("findAdmin 진입, memberCode: {}", memberCode);
        Member memberInfo = memberRepository.findById(memberCode).orElseThrow();
        log.info("memberInfo: {}", memberInfo);
        Admin adminInfo = adminRepository.findById(memberCode).orElseThrow();
        log.info("adminInfo: {}", adminInfo);

        return AdminProfileRes.builder()
                .memberCode(memberInfo.getMemberCode())
                .role(role.getCode())
                // 기본 정보
                .name(memberInfo.getName())
                .email(memberInfo.getEmail())
                .address(memberInfo.getAddress())
                .pic(memberInfo.getPic())
                .birth(memberInfo.getBirth())
                .tel(memberInfo.getTel())
                .emergencyTel(memberInfo.getEmergencyTel())
                .postcode(memberInfo.getPostcode())
                .detailAddress(memberInfo.getDetailAddress())
                .entryDate(memberInfo.getEntryDate())
                .exitDate(memberInfo.getExitDate())
                // 학사 정보
                .status(adminInfo.getStatus().getCode())
                .build();
    }

    @Transactional
    public void updateMyProfile(Long memberCode, EnumMemberRole role,
                                MemberUpdateReq req, MultipartFile pic) {
        Member member = memberRepository.findById(memberCode).orElseThrow();

        // 사진 처리
        String savedPicFileName = null;
        if (pic != null) {
            // 기존 사진 삭제
            if (member.getPic() != null) {
                try {
                    myFileUtil.deleteFile(String.format("member/%s/%s", memberCode, member.getPic()));
                } catch (Exception e) {
                    log.warn("기존 파일 삭제 실패: {}", e.getMessage());
                }
            }
            savedPicFileName = myFileUtil.makeRandomFileName(pic);
            String middlePath = "member/" + memberCode;
            myFileUtil.makeFolders(middlePath);
            String fullFilePath = String.format("%s/%s", middlePath, savedPicFileName);
            try {
                myFileUtil.transferTo(pic, fullFilePath);
            } catch (IOException e) {
                log.error("파일 저장 실패: {}", e.getMessage());
            }
        }

        // 공통 필드 업데이트
        member.updateCommon(
                req.getTel(),
                req.getEmergencyTel(),
                req.getPostcode(),
                req.getAddress(),
                req.getDetailAddress(),
                savedPicFileName,
                req.getEmail()
        );

        // 교수 연구실 업데이트
        if (role == EnumMemberRole.PROFESSOR) {
            Professor professor = professorRepository.findById(memberCode).orElseThrow();
            professor.updateLab(req.getLabBuilding(), req.getLabRoom(), req.getLabTel());
        }

        // AuthMemberEvent Outbox 저장
        AuthMemberEvent authEvent = AuthMemberEvent.builder()
                .memberCode(memberCode)
                .email(member.getEmail())
                .eventType(EventType.E_UPDATED)
                .build();

        saveToOutbox(memberTopic.AUTH_MEMBER, member.getMemberCode(), authEvent);

    }


    private void saveToOutbox(String topic, Long aggregateId, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            Outbox outbox = Outbox.builder()
                    .topic(topic)
                    .aggregateId(aggregateId)
                    .eventType(EventType.E_CREATED.name())
                    .payload(payload)
                    .build();
            outboxRepository.save(outbox);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Outbox 직렬화 실패", e);
        }
    }
}
