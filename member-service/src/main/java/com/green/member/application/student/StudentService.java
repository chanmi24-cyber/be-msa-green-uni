package com.green.member.application.student;

import com.green.common.constants.EventType;
import com.green.common.enumcode.EnumApprovalStatus;
import com.green.common.enumcode.EnumMajorType;
import com.green.common.enumcode.EnumMemberRole;
import com.green.common.exception.BusinessException;
import com.green.common.kafka.member.GpaRequestEvent;
import com.green.common.kafka.member.MemberTopic;
import com.green.member.application.OutboxService;
import com.green.member.application.member.MemberRepository;
import com.green.member.application.member.model.MemberProfileRes;
import com.green.member.application.schedule.SchedulePeriodValidator;
import com.green.member.application.student.model.*;
import com.green.member.configuration.MyFileUtil;
import com.green.member.entity.cache.MajorCache;
import com.green.member.entity.member.Member;
import com.green.member.entity.student.MajorRequest;
import com.green.member.entity.student.Student;
import com.green.member.entity.student.StudentHistory;
import com.green.member.entity.student.StudentMajor;
import com.green.member.exception.MemberErrorCode;
import com.green.member.application.major.MajorCacheRepository;
import com.green.member.exception.RequestErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
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
    private final MajorRequestRepository majorRequestRepository;
    private final OutboxService outboxService;
    private final SchedulePeriodValidator schedulePeriodValidator;
    private final MyFileUtil myFileUtil;

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
        if (!studentRepository.existsById(memberCode)) {
            throw new BusinessException(MemberErrorCode.STUDENT_NOT_FOUND);
        }
        List<StudentHistory> histories =
                studentHistoryRepository.findByStudent_MemberCodeOrderByCreatedAtDesc(memberCode);
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

    // 학생 전공 변경 신청
    @Transactional
    public void requestMajor(StudentMajorReq req, MultipartFile file, Long memberCode){
        // 회원 조회
        Student student = studentRepository.findById(memberCode).orElseThrow(() -> new BusinessException(MemberErrorCode.STUDENT_NOT_FOUND));

        // 전공 변경 신청 기간 체크
        schedulePeriodValidator.checkMajorChange();

        // 중복 신청 방지
        if (majorRequestRepository.existsByStudent_MemberCodeAndTypeAndStatus( memberCode, req.getType(), EnumApprovalStatus.PENDING)) {
            throw new BusinessException(RequestErrorCode.ALREADY_PENDING_REQUEST);
        }

        // 전공 검증
        majorCacheRepository.findById(req.getTargetMajorId()) // 존재하지 않는 학과라면
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MAJOR_NOT_FOUND));
        List<StudentMajor> activeMajors = studentMajorRepository.findByStudent_MemberCodeAndIsActiveTrue(memberCode);
        boolean alreadyInMajor = activeMajors.stream()
                .anyMatch(m -> m.getMajorId().equals(req.getTargetMajorId()));
        if (alreadyInMajor) { // 본인의 학과를 재신청한다면
            throw new BusinessException(RequestErrorCode.ALREADY_IN_MAJOR);
        }

        // 파일 검증 및 처리
        if (file != null) {
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new BusinessException(RequestErrorCode.FILE_TOO_LARGE);
            }
            if (!"application/pdf".equals(file.getContentType())) {
                throw new BusinessException(RequestErrorCode.INVALID_FILE_TYPE);
            }
        }
        String savedFileName = file == null ? null : myFileUtil.makeRandomFileName(file);

        // major_request 저장
        MajorRequest request = MajorRequest.builder()
                .student(student)
                .type(req.getType())
                .targetMajorId(req.getTargetMajorId())
                .reason(req.getReason())
                .file(savedFileName)
                .gpa(BigDecimal.ZERO)
                .build();
        MajorRequest newRequest = majorRequestRepository.save(request);

        // GPA 조회 요청 이벤트 발행
        outboxService.saveToOutbox(
                MemberTopic.GPA_REQUEST,
                newRequest.getRequestId(),
                GpaRequestEvent.builder()
                        .requestId(newRequest.getRequestId())
                        .studentCode(memberCode)
                        .eventType(EventType.E_CREATED)
                        .build()
        );

        // 파일 저장
        if (file != null) {
            String middlePath = "member/major/request/" + memberCode;
            myFileUtil.makeFolders(middlePath);
            String fullFilePath = String.format("%s/%s", middlePath, savedFileName);
            try {
                myFileUtil.transferTo(file, fullFilePath);
            } catch (IOException e) {
                request.setFile(null);
                log.error("파일 저장 실패: {}", e.getMessage());
            }
        }
    }

    @Transactional
    public void deleteMajorRequest(Long requestId, Long memberCode){
        MajorRequest request = majorRequestRepository.findByRequestIdAndStudent_MemberCode(requestId, memberCode)
                .orElseThrow(() -> new BusinessException(RequestErrorCode.NOT_MAJOR_REQUEST)); // requestId와 memberCode 일치
        if (request.getStatus() != EnumApprovalStatus.PENDING) { // 대기 상태일때만 취소 가능
            throw new BusinessException(RequestErrorCode.NOT_CANCELLABLE);
        }
        // 신청 취소
        request.cancel();
        // 첨부파일 있었다면 삭제
        if (request.getFile() != null) {
            try {
                myFileUtil.deleteFile(String.format("member/major/request/%s/%s", memberCode, request.getFile()));
            } catch (Exception e) {
                log.warn("기존 파일 삭제 실패: {}", e.getMessage());
            }
        }
    }
    // 학생 전공 변경 신청 목록 조회
    @Transactional(readOnly = true)
    public List<MajorRequestRes> findMajorRequests(Long memberCode){
        if (!studentRepository.existsById(memberCode)) {
            throw new BusinessException(MemberErrorCode.STUDENT_NOT_FOUND);
        }
        List<MajorRequest> requests =
               majorRequestRepository.findByStudent_MemberCodeOrderByCreatedAtDesc(memberCode);
        return requests.stream()
                .map( h -> {
                    String majorName = majorCacheRepository.findById(h.getTargetMajorId())
                            .map(MajorCache::getName)
                            .orElse(null);
                    MajorRequestRes res = new MajorRequestRes();
                    res.setRequestId(h.getRequestId());
                    res.setType(h.getType());
                    res.setTargetMajorName(majorName);
                    res.setStatus(h.getStatus());
                    res.setCreatedAt(h.getCreatedAt());
                    return res;
                })
                .toList();
    }
    // 학생 전공 변경 신청서 상세 조회
    @Transactional(readOnly = true)
    public MajorRequestDetailRes findMajorRequest (Long requestId, Long memberCode){
        MajorRequest request = majorRequestRepository.findByRequestIdAndStudent_MemberCode( requestId, memberCode )
                .orElseThrow(() -> new BusinessException(RequestErrorCode.NOT_MAJOR_REQUEST));
        String majorName = majorCacheRepository.findById(request.getTargetMajorId())
                .map(MajorCache::getName)
                .orElse(null);
        return MajorRequestDetailRes.builder()
                .requestId(requestId)
                .type(request.getType())
                .targetMajorName(majorName)
                .status(request.getStatus())
                .gpa(request.getGpa())
                .reason(request.getReason())
                .file(request.getFile())
                .approveReason(request.getApproveReason())
                .rejectReason(request.getRejectReason())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
