package com.green.member.application.member;

import com.green.common.auth.MemberContext;
import com.green.common.enumcode.EnumMemberRole;
import com.green.common.enumcode.EnumMajorType;
import com.green.member.application.admin.AdminRepository;
import com.green.member.application.member.model.MemberProfileRes;
import com.green.member.application.professor.ProfessorRepository;
import com.green.member.application.student.StudentMajorRepository;
import com.green.member.application.student.StudentRepository;
import com.green.member.application.student.model.StudentProfileRes;
import com.green.member.entity.cache.MajorCache;
import com.green.member.entity.member.Member;
import com.green.member.entity.student.Student;
import com.green.member.entity.student.StudentMajor;
import com.green.member.repository.MajorCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public MemberProfileRes getMyProfile(Long memberCode, EnumMemberRole role){
        MemberProfileRes memberProfile = switch (role) {
            case STUDENT   -> findStudent(memberCode);
            case PROFESSOR -> findProfessor(memberCode);
            case ADMIN     -> findAdmin(memberCode);
        };
        return memberProfile;
    }

    public StudentProfileRes findStudent(Long memberCode){
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
                .role(MemberContext.get().role())
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
                .status(studentInfo.getStatus())
                .build();
    }

    public MemberProfileRes findProfessor(Long memberCode){
        return null;
    }

    public MemberProfileRes findAdmin(Long memberCode){
        return null;
    }


}
