package com.green.member.application.student;

import com.green.common.enumcode.EnumMajorType;
import com.green.common.enumcode.EnumMemberRole;
import com.green.common.exception.BusinessException;
import com.green.member.application.member.MemberRepository;
import com.green.member.application.student.model.StudentHistoryRes;
import com.green.member.application.student.model.StudentProfileRes;
import com.green.member.entity.cache.MajorCache;
import com.green.member.entity.member.Member;
import com.green.member.entity.student.Student;
import com.green.member.entity.student.StudentHistory;
import com.green.member.entity.student.StudentMajor;
import com.green.member.exception.MemberErrorCode;
import com.green.member.application.major.MajorCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {
    private final MemberRepository memberRepository;
    private final MajorCacheRepository majorCacheRepository;
    private final StudentRepository studentRepository;
    private final StudentMajorRepository studentMajorRepository;
    private final StudentHistoryRepository studentHistoryRepository;

    // 학생 정보 조회
    public StudentProfileRes findStudent(Long memberCode, EnumMemberRole role){
        Member memberInfo = memberRepository.findById(memberCode).orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
        Student studentInfo = studentRepository.findById(memberCode).orElseThrow(() -> new BusinessException(MemberErrorCode.STUDENT_NOT_FOUND));
        List<StudentMajor> majors = studentMajorRepository.findByStudent_MemberCodeAndIsActiveTrue(memberCode);

        StudentMajor mainMajor = majors.stream()
                .filter(m -> m.getType() == EnumMajorType.PRIMARY)
                .findFirst()
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MAJOR_NOT_FOUND));
        StudentMajor subMajor = majors.stream()
                .filter(m -> m.getType() == EnumMajorType.MINOR)
                .findFirst()
                .orElse(null);
        MajorCache mainMajorCache = majorCacheRepository.findById(mainMajor.getMajorId()).orElseThrow(() -> new BusinessException(MemberErrorCode.MAJOR_NOT_FOUND));
        String subMajorName = null;
        if (subMajor != null) {
            MajorCache subMajorCache = majorCacheRepository.findById(subMajor.getMajorId()).orElseThrow(() -> new BusinessException(MemberErrorCode.MAJOR_NOT_FOUND));
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

    // 학생 상태 변경 이력 조회
    @Transactional(readOnly = true)
    public List<StudentHistoryRes> findStudentHistory(Long memberCode){
        List<StudentHistory> histories =
                studentHistoryRepository.findByStudent_MemberCodeOrderByCreatedAtDesc(memberCode);
        if (histories.isEmpty()) {
            throw new BusinessException(MemberErrorCode.STUDENT_NOT_FOUND);
        }
        return histories.stream()
                .map( h -> {
                    StudentHistoryRes res = new StudentHistoryRes();
                    res.setChangeType(h.getChangeType());
                    res.setOldStatus(h.getOldStatus());
                    res.setNewStatus(h.getNewStatus());
                    res.setStartDate(h.getStartDate());
                    res.setEndDate(h.getEndDate());
                    res.setReason(h.getReason());
                    res.setReturnYear(h.getReturnYear());
                    res.setReturnSemester(h.getReturnSemester());
                    res.setCreatedAt(h.getCreatedAt());
                    return res;
                })
                .toList()
                ;
    }
}
