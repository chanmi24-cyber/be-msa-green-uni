package com.green.member.application.student;

import com.green.common.enumcode.EnumApprovalStatus;
import com.green.common.enumcode.EnumMajorType;
import com.green.common.enumcode.EnumMemberRole;
import com.green.common.enumcode.EnumStudentStatus;
import com.green.common.exception.BusinessException;
import com.green.common.exception.FileErrorCode;
import com.green.common.file.FileService;
import com.green.member.application.major.MajorRequestRepository;
import com.green.member.application.major.model.StudentMajorHistoryRes;
import com.green.member.application.major.model.StudentMajorRequestDetailRes;
import com.green.member.application.major.model.StudentMajorRequestListRes;
import com.green.member.application.major.model.StudentMajorRequestReq;
import com.green.member.application.member.MemberRepository;
import com.green.member.application.schedule.SchedulePeriodValidator;
import com.green.member.application.status.StatusRequestRepository;
import com.green.member.application.status.model.StudentStatusRequestDetailRes;
import com.green.member.application.status.model.StudentStatusRequestListRes;
import com.green.member.application.status.model.StudentStatusRequestReq;
import com.green.member.application.student.model.*;
import com.green.member.application.student.model.StudentDashboardRequestRes;
import com.green.member.entity.cache.MajorCache;
import com.green.member.entity.member.Member;
import com.green.member.entity.student.*;
import com.green.member.enumcode.EnumMajorRequestType;
import com.green.member.enumcode.EnumStatusRequestType;
import com.green.member.exception.MemberErrorCode;
import com.green.member.application.major.MajorCacheRepository;
import com.green.member.exception.RequestErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final SchedulePeriodValidator schedulePeriodValidator;
    private final FileService fileService;
    private final StatusRequestRepository statusRequestRepository;

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
                    res.setReason(h.getNote());
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
    public void requestMajor(StudentMajorRequestReq req, MultipartFile file, Long memberCode){
        // 회원 조회
        Student student = studentRepository.findById(memberCode).orElseThrow(() -> new BusinessException(MemberErrorCode.STUDENT_NOT_FOUND));

        // 재학/휴학 상태만 신청 가능
        if (student.getStatus() != EnumStudentStatus.ENROLLED && student.getStatus() != EnumStudentStatus.ABSENCE) {
            throw new BusinessException(RequestErrorCode.INELIGIBLE_STUDENT_STATUS);
        }

        // 편입생은 전과 신청 불가
        if (req.getType() == EnumMajorRequestType.TRANSFER && student.getIsTransfer()) {
            throw new BusinessException(RequestErrorCode.TRANSFER_STUDENT_CANNOT_TRANSFER);
        }

        // 전공 변경 신청 기간 체크
        schedulePeriodValidator.checkMajorChange();

        // 중복 신청 방지 (타입 무관하게 PENDING 신청이 하나라도 있으면 차단)
        if (majorRequestRepository.existsByStudent_MemberCodeAndStatus(memberCode, EnumApprovalStatus.PENDING)) {
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

        // 파일 검증 및 디스크 저장 (DB 저장 전에 처리하여 유효하지 않은 파일이 DB에 남지 않도록 함)
        String savedFileName = null;
        if (file != null) {
            savedFileName = fileService.save(file, "request/major/" + memberCode, FileService.ALLOWED_DOCUMENT_EXTENSIONS);
        }

        // 클라이언트 제공 파일명의 경로 구분자를 제거하여 파일명만 추출 (path traversal 방지)
        String originalFileName = null;
        if (file != null) {
            String rawName = file.getOriginalFilename();
            originalFileName = (rawName != null)
                    ? Paths.get(rawName).getFileName().toString()
                    : null;
        }

        // major_request 저장 (신청 당시 학년,학기,전공 함께 기록)
        MajorRequest request = MajorRequest.builder()
                .student(student)
                .type(req.getType())
                .targetMajorId(req.getTargetMajorId())
                .reason(req.getReason())
                .file(savedFileName)
                .originalFileName(originalFileName)
                .gpa(BigDecimal.ZERO)
                .academicYear(student.getAcademicYear())
                .semester(student.getSemester())
                .currentMajorId(activeMajors.stream()
                        .filter(m -> m.getType() == EnumMajorType.PRIMARY)
                        .map(StudentMajor::getMajorId)
                        .findFirst()
                        .orElseThrow(() -> new BusinessException(MemberErrorCode.MAJOR_NOT_FOUND)))
                .currentMinorId(activeMajors.stream()
                        .filter(m -> m.getType() == EnumMajorType.MINOR)
                        .map(StudentMajor::getMajorId).findFirst().orElse(null))
                .build();
        majorRequestRepository.save(request);
    }
    // 내 전공 변경 신청 취소
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
            fileService.delete(String.format("request/major/%s/%s", memberCode, request.getFile()));
        }
    }
    // 내 전공 변경 신청 목록 조회
    @Transactional(readOnly = true)
    public List<StudentMajorRequestListRes> findMajorRequests(Long memberCode){
        if (!studentRepository.existsById(memberCode)) {
            throw new BusinessException(MemberErrorCode.STUDENT_NOT_FOUND);
        }
        return majorRequestRepository.findStudentMajorRequests(memberCode);
    }

    // 내 전공 변경 이력 조회
    @Transactional(readOnly = true)
    public List<StudentMajorHistoryRes> findMajorHistory(Long memberCode) {
        if (!studentRepository.existsById(memberCode)) {
            throw new BusinessException(MemberErrorCode.STUDENT_NOT_FOUND);
        }
        return majorRequestRepository.findMajorHistoryByStudentCode(memberCode);
    }

    // 내 전공 변경 신청서 상세 조회
    @Transactional(readOnly = true)
    public StudentMajorRequestDetailRes findMajorRequest(Long requestId, Long memberCode){
        return majorRequestRepository.findStudentMajorRequestDetail(requestId, memberCode)
                .orElseThrow(() -> new BusinessException(RequestErrorCode.NOT_MAJOR_REQUEST));
    }

    // 내 전공 변경 신청서 파일 다운로드
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> findMajorRequestFile(Long requestId, Long memberCode) {
        // requestId + memberCode 조합으로 타인의 파일 접근 차단
        MajorRequest request = majorRequestRepository.findByRequestIdAndStudent_MemberCode(requestId, memberCode)
                .orElseThrow(() -> new BusinessException(RequestErrorCode.NOT_MAJOR_REQUEST));
        if (request.getFile() == null) {
            throw new BusinessException(FileErrorCode.FILE_NOT_FOUND);
        }
        // DB의 UUID 파일명으로 경로 구성 (클라이언트 입력값 미사용 → path traversal 불가)
        String filePath = String.format("request/major/%s/%s", memberCode, request.getFile());
        return fileService.buildDownloadResponse(filePath, request.getOriginalFileName());
    }

    // 학생 학적 변경 신청
    @Transactional
    public void requestStatus(StudentStatusRequestReq req, MultipartFile file, Long memberCode){
        Student student = studentRepository.findById(memberCode).orElseThrow(() -> new BusinessException(MemberErrorCode.STUDENT_NOT_FOUND));

        // 재학/휴학 상태만 신청 가능 (졸업, 자퇴, 퇴학 등은 차단)
        if (student.getStatus() != EnumStudentStatus.ENROLLED && student.getStatus() != EnumStudentStatus.ABSENCE) {
            throw new BusinessException(RequestErrorCode.INELIGIBLE_STUDENT_STATUS);
        }

        // 중복 신청 방지 (PENDING 신청이 하나라도 있으면 차단)
        if (statusRequestRepository.existsByStudent_MemberCodeAndStatus(memberCode, EnumApprovalStatus.PENDING)) {
            throw new BusinessException(RequestErrorCode.ALREADY_PENDING_REQUEST);
        }

        // 자퇴: 재학, 휴학 상태 모두 가능
        // 휴학: 재학 상태만 가능
        if (req.getType() == EnumStatusRequestType.ABSENCE && student.getStatus() != EnumStudentStatus.ENROLLED) {
            throw new BusinessException(RequestErrorCode.INVALID_STATUS_REQUEST_TYPE);
        }
        // 복학: 휴학 상태만 가능
        if (req.getType() == EnumStatusRequestType.RETURN && student.getStatus() != EnumStudentStatus.ABSENCE) {
            throw new BusinessException(RequestErrorCode.INVALID_STATUS_REQUEST_TYPE);
        }

        // 파일 검증 및 디스크 저장
        String savedFileName = null;
        if (file != null) {
            savedFileName = fileService.save(file, "request/status/" + memberCode, FileService.ALLOWED_DOCUMENT_EXTENSIONS);
        }
        String originalFileName = null;
        if (file != null) {
            String rawName = file.getOriginalFilename();
            originalFileName = (rawName != null)
                    ? Paths.get(rawName).getFileName().toString()
                    : null;
        }

        StatusRequest request = StatusRequest.builder()
                .student(student)
                .type(req.getType())
                .reason(req.getReason())
                .file(savedFileName)
                .originalFileName(originalFileName)
                .academicYear(student.getAcademicYear())
                .semester(student.getSemester())
                .startDate(req.getStartDate())
                .returnYear(req.getReturnYear())
                .returnSemester(req.getReturnSemester())
                .build();
        statusRequestRepository.save(request);
    }
    // 내 학적 변경 신청 취소
    @Transactional
    public void deleteStatusRequest(Long requestId, Long memberCode){
        StatusRequest request = statusRequestRepository.findByRequestIdAndStudent_MemberCode(requestId, memberCode)
                .orElseThrow(() -> new BusinessException(RequestErrorCode.NOT_STATUS_REQUEST));
        if (request.getStatus() != EnumApprovalStatus.PENDING) {
            throw new BusinessException(RequestErrorCode.NOT_CANCELLABLE);
        }
        request.cancel();
        if (request.getFile() != null) {
            fileService.delete(String.format("request/status/%s/%s", memberCode, request.getFile()));
        }
    }
    // 내 학적 변경 신청 목록 조회
    @Transactional(readOnly = true)
    public List<StudentStatusRequestListRes> findStatusRequests(Long memberCode){
        if (!studentRepository.existsById(memberCode)) {
            throw new BusinessException(MemberErrorCode.STUDENT_NOT_FOUND);
        }
        return statusRequestRepository.findStudentStatusRequests(memberCode);
    }

    // 대시보드: 학적변경 + 전공변경 신청 통합 목록 조회 (createdAt DESC, size 제한)
    @Transactional(readOnly = true)
    public List<StudentDashboardRequestRes> findDashboardRequests(Long memberCode, int size) {
        if (!studentRepository.existsById(memberCode)) {
            throw new BusinessException(MemberErrorCode.STUDENT_NOT_FOUND);
        }
        List<StudentDashboardRequestRes> merged = new ArrayList<>();

        statusRequestRepository.findStudentStatusRequests(memberCode).forEach(r ->
                merged.add(new StudentDashboardRequestRes(
                        r.getRequestId(), "STATUS", r.getType(), null,
                        r.getStatus(), r.getAcademicYear(), r.getSemester(), r.getCreatedAt()
                ))
        );
        majorRequestRepository.findStudentMajorRequests(memberCode).forEach(r ->
                merged.add(new StudentDashboardRequestRes(
                        r.getRequestId(), "MAJOR", r.getType(), r.getTargetMajorName(),
                        r.getStatus(), r.getAcademicYear(), r.getSemester(), r.getCreatedAt()
                ))
        );

        return merged.stream()
                .sorted(Comparator.comparing(StudentDashboardRequestRes::getCreatedAt).reversed())
                .limit(size)
                .toList();
    }
    // 내 학적 변경 신청서 상세 조회
    @Transactional(readOnly = true)
    public StudentStatusRequestDetailRes findStatusRequest(Long requestId, Long memberCode){
        return statusRequestRepository.findStudentStatusRequestDetail(requestId, memberCode)
                .orElseThrow(() -> new BusinessException(RequestErrorCode.NOT_STATUS_REQUEST));
    }
    // 내 학적 변경 신청서 파일 다운로드
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> findStatusRequestFile(Long requestId, Long memberCode) {
        // requestId + memberCode 조합으로 타인의 파일 접근 차단
        StatusRequest request = statusRequestRepository.findByRequestIdAndStudent_MemberCode(requestId, memberCode)
                .orElseThrow(() -> new BusinessException(RequestErrorCode.NOT_STATUS_REQUEST));
        if (request.getFile() == null) {
            throw new BusinessException(FileErrorCode.FILE_NOT_FOUND);
        }
        // DB의 UUID 파일명으로 경로 구성
        String filePath = String.format("request/status/%s/%s", memberCode, request.getFile());
        return fileService.buildDownloadResponse(filePath, request.getOriginalFileName());
    }

}
