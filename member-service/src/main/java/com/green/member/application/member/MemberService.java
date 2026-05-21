package com.green.member.application.member;

import com.green.common.constants.EventType;
import com.green.common.constants.UpdateType;
import com.green.common.enumcode.EnumMemberRole;
import com.green.common.exception.BusinessException;
import com.green.common.kafka.auth.AuthMemberEvent;
import com.green.common.kafka.member.StudentEvent;
import com.green.common.kafka.member.MemberTopic;
import com.green.member.application.OutboxService;
import com.green.member.application.admin.AdminRepository;
import com.green.member.application.admin.model.AdminProfileRes;
import com.green.member.application.file.FileService;
import com.green.member.application.member.model.MemberProfileRes;
import com.green.member.application.member.model.MemberUpdateReq;
import com.green.member.application.professor.ProfessorRepository;
import com.green.member.application.professor.ProfessorService;
import com.green.member.application.student.StudentService;
import com.green.member.entity.member.Admin;
import com.green.member.entity.member.Member;
import com.green.member.entity.professor.Professor;
import com.green.member.exception.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final ProfessorRepository professorRepository;
    private final FileService fileService;
    private final MemberHistoryService memberHistoryService;
    private final OutboxService outboxService;
    private final StudentService studentService;
    private final ProfessorService professorService;
    private final AdminRepository adminRepository;

    // 내 정보 조회
    @Transactional(readOnly = true)
    public MemberProfileRes getMyProfile(Long memberCode, EnumMemberRole role){
        MemberProfileRes memberProfile = switch (role) {
            case STUDENT   -> studentService.findStudent(memberCode, role);
            case PROFESSOR -> professorService.findProfessor(memberCode, role);
            case ADMIN     -> findAdmin(memberCode, role);
        };
        return memberProfile;
    }

    // 관리자 정보 조회
    public AdminProfileRes findAdmin(Long memberCode, EnumMemberRole role){
        log.info("findAdmin 진입, memberCode: {}", memberCode);
        Member memberInfo = memberRepository.findById(memberCode).orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
        log.info("memberInfo: {}", memberInfo);
        Admin adminInfo = adminRepository.findById(memberCode).orElseThrow(() -> new BusinessException(MemberErrorCode.ADMIN_NOT_FOUND));
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

    // 내 정보 수정
    @Transactional
    public void updateMyProfile(Long memberCode, EnumMemberRole role,
                                MemberUpdateReq req, MultipartFile pic) {
        Member member = memberRepository.findById(memberCode).orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 사진 처리
        String savedPicFileName = null;
        if (pic != null) {
            // 기존 사진 삭제
            if (member.getPic() != null) {
                fileService.delete(String.format("member/%s/%s", memberCode, member.getPic()));
            }
            // 이미지 파일 검증 후 저장 (JPG, JPEG, PNG만 허용)
            savedPicFileName = fileService.save(pic, "member/" + memberCode, FileService.ALLOWED_IMAGE_EXTENSIONS);
        }

        String oldEmail = member.getEmail();
        String oldTel = member.getTel();
        String oldEmergencyTel = member.getEmergencyTel();
        String oldPostcode = member.getPostcode();
        String oldAddress = member.getAddress();
        String oldDetailAddress = member.getDetailAddress();
        String oldPic = member.getPic();

        log.info("oldEmail: {}, reqEmail: {}", oldEmail, req.getEmail());

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

        // 이메일 처리
        if(req.getEmail() != null && !req.getEmail().equals(oldEmail)){
            if (memberRepository.existsByEmail(req.getEmail())) {
                throw new BusinessException(MemberErrorCode.DUPLICATE_EMAIL);
            }

            // AuthMemberEvent Outbox 저장
            AuthMemberEvent authEvent = AuthMemberEvent.builder()
                    .memberCode(memberCode)
                    .email(req.getEmail())
                    .eventType(EventType.E_UPDATED)
                    .updateType(UpdateType.EMAIL)
                    .build();
            outboxService.saveToOutbox(MemberTopic.AUTH_MEMBER, member.getMemberCode(), authEvent);

            // StudentEvent Outbox 저장
            if (role == EnumMemberRole.STUDENT) {
                StudentEvent studentEvent = StudentEvent.builder()
                        .memberCode(member.getMemberCode())
                        .email(req.getEmail())
                        .eventType(EventType.E_UPDATED)
                        .updateType(UpdateType.EMAIL)
                        .build();
                outboxService.saveToOutbox(MemberTopic.STUDENT, member.getMemberCode(), studentEvent);
            }
        }

        // MemberHistory 저장을 위한 변경된 필드만 수집
        Map<String, Object> before = new LinkedHashMap<>();
        if (req.getEmail() != null && !req.getEmail().equals(oldEmail)) before.put("email", oldEmail);
        if (req.getTel() != null && !req.getTel().equals(oldTel)) before.put("tel", oldTel);
        if (req.getEmergencyTel() != null && !req.getEmergencyTel().equals(oldEmergencyTel)) before.put("emergencyTel", oldEmergencyTel);
        if (req.getPostcode() != null && !req.getPostcode().equals(oldPostcode)) before.put("postcode", oldPostcode);
        if (req.getAddress() != null && !req.getAddress().equals(oldAddress)) before.put("address", oldAddress);
        if (req.getDetailAddress() != null && !req.getDetailAddress().equals(oldDetailAddress)) before.put("detailAddress", oldDetailAddress);
        if (savedPicFileName != null && !savedPicFileName.equals(oldPic)) before.put("pic", oldPic);

        // 교수 연구실 업데이트
        if (role == EnumMemberRole.PROFESSOR) {
            Professor professor = professorRepository.findById(memberCode).orElseThrow(() -> new BusinessException(MemberErrorCode.PROFESSOR_NOT_FOUND));
            String oldLabBuilding = professor.getLabBuilding() != null ? professor.getLabBuilding().getCode() : null;
            String oldLabRoom = professor.getLabRoom();
            String oldLabTel = professor.getLabTel();

            professor.updateLab(req.getLabBuilding(), req.getLabRoom(), req.getLabTel());

            if (req.getLabBuilding() != null && !req.getLabBuilding().getCode().equals(oldLabBuilding)) before.put("labBuilding", oldLabBuilding);
            if (req.getLabRoom() != null && !req.getLabRoom().equals(oldLabRoom)) before.put("labRoom", oldLabRoom);
            if (req.getLabTel() != null && !req.getLabTel().equals(oldLabTel)) before.put("labTel", oldLabTel);
        }
        memberHistoryService.save(memberCode, memberCode, before);

    }

}
