package com.green.member.application.professor;

import com.green.common.enumcode.EnumMemberRole;
import com.green.member.application.member.MemberRepository;
import com.green.member.application.professor.model.ProfessorHistoryRes;
import com.green.member.application.professor.model.ProfessorProfileRes;
import com.green.member.entity.cache.MajorCache;
import com.green.member.entity.member.Member;
import com.green.member.entity.professor.Professor;
import com.green.member.repository.MajorCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfessorService {
    private final MemberRepository memberRepository;
    private final MajorCacheRepository majorCacheRepository;
    private final ProfessorRepository professorRepository;
    private final ProfessorHistoryRepository professorHistoryRepository;

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

    // 교수 상태 변경 이력 조회
    public List<ProfessorHistoryRes> findStatusHistory(Long memberCode){
        return professorHistoryRepository.findByProfessor_MemberCode(memberCode)
                .stream()
                .map( h -> {
                    ProfessorHistoryRes res = new ProfessorHistoryRes();
                    res.setChangeType(h.getChangeType());
                    res.setOldStatus(h.getOldStatus());
                    res.setNewStatus(h.getNewStatus());
                    res.setOldPosition(h.getOldPosition());
                    res.setNewPosition(h.getNewPosition());
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
