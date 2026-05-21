package com.green.member.application.student;

import com.green.common.constants.EventType;
import com.green.common.enumcode.EnumApprovalStatus;
import com.green.common.enumcode.EnumMajorType;
import com.green.common.enumcode.EnumMemberRole;
import com.green.common.exception.BusinessException;
import com.green.common.exception.FileErrorCode;
import com.green.common.kafka.member.GpaRequestEvent;
import com.green.common.kafka.member.MemberTopic;
import com.green.member.application.OutboxService;
import com.green.common.file.FileService;
import com.green.member.application.member.MemberRepository;
import com.green.member.application.schedule.SchedulePeriodValidator;
import com.green.member.application.student.model.*;
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

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
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
    private final FileService fileService;

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
    public List<MajorRequestRes> findMajorRequests(Long memberCode){
        if (!studentRepository.existsById(memberCode)) {
            throw new BusinessException(MemberErrorCode.STUDENT_NOT_FOUND);
        }
        return majorRequestRepository.findStudentMajorRequests(memberCode);
    }

    // 내 전공 변경 신청서 상세 조회
    @Transactional(readOnly = true)
    public MajorRequestDetailRes findMajorRequest(Long requestId, Long memberCode){
        return majorRequestRepository.findStudentMajorRequestDetail(requestId, memberCode)
                .orElseThrow(() -> new BusinessException(RequestErrorCode.NOT_MAJOR_REQUEST));
    }

    // 내 전공 변경 신청서 파일 다운로드
    public ResponseEntity<Resource> findMajorRequestFile(Long requestId, Long memberCode) {
        // 신청서 소유권 확인: requestId + memberCode 조합으로 타인의 파일 접근 차단
        MajorRequest request = majorRequestRepository.findByRequestIdAndStudent_MemberCode(requestId, memberCode)
                .orElseThrow(() -> new BusinessException(RequestErrorCode.NOT_MAJOR_REQUEST));

        if (request.getFile() == null) {
            throw new BusinessException(FileErrorCode.FILE_NOT_FOUND);
        }

        // DB에 저장된 UUID 기반 파일명으로 경로 구성 (클라이언트 입력값 미사용 → path traversal 불가)
        String filePath = String.format("request/major/%s/%s", memberCode, request.getFile());
        Resource resource = fileService.getResource(filePath);

        if (!resource.exists()) {
            throw new BusinessException(FileErrorCode.FILE_NOT_FOUND);
        }

        // 다운로드 파일명: 저장 시 살균된 원본 파일명 우선, 없으면 UUID 파일명 사용
        String downloadName = request.getOriginalFileName() != null
                ? request.getOriginalFileName()
                : request.getFile();
        // RFC 5987 인코딩으로 한글/특수문자 파일명 안전 처리
        String encodedName = URLEncoder.encode(downloadName, StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                // 브라우저가 파일을 직접 실행하지 않도록 OCTET_STREAM으로 강제 다운로드
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

}
