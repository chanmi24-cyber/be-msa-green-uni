package com.green.core.application.tuition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.common.enumcode.EnumScheduleType;
import com.green.common.enumcode.EnumChangeType; // 이력 보존용 추가
import com.green.common.exception.BusinessException;
import com.green.core.application.major.MajorRepository;
import com.green.core.application.scholarship.ScholarshipRepository;
import com.green.core.application.tuition.model.TuitionReq;
import com.green.core.application.tuition.model.TuitionRes;
import com.green.core.application.tuition.model.TuitionMailEvent;
import com.green.core.entity.cache.ScheduleCache;
import com.green.core.entity.cache.StudentCache;
import com.green.core.entity.major.Major;
import com.green.core.entity.scholarship.Scholarship;
import com.green.core.entity.tuition.Tuition;
import com.green.core.entity.tuition.TuitionMailLog;
import com.green.core.entity.tuition.TuitionPolicy;
import com.green.core.entity.tuition.TuitionPolicyHistory; // 이력 엔티티 추가
import com.green.core.enumcode.EnumTuitionStatus;
import com.green.core.repository.ScheduleCacheRepository;
import com.green.core.repository.StudentCacheRepository;
import com.green.core.scheduleValidator.SchedulePeriodValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TuitionService {

    private final TuitionRepository tuitionRepository;
    private final TuitionPolicyRepository tuitionPolicyRepository;
    private final TuitionPolicyHistoryRepository tuitionPolicyHistoryRepository; // 이력 레포지토리 주입 추가
    private final TuitionMailLogRepository tuitionMailLogRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final SchedulePeriodValidator schedulePeriodValidator;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ScheduleCacheRepository scheduleCacheRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StudentCacheRepository studentCacheRepository;
    private final MajorRepository majorRepository;

    // ==========================================
    // [학생] 서비스 로직
    // ==========================================

    public List<TuitionRes> getStudentTuitionList(Long studentCode) {
        return tuitionRepository.findByStudentCodeOrderByYearDescSemesterDesc(studentCode).stream()
                .map(TuitionRes::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public TuitionRes.MyTuitionDetailRes getStudentTuitionDetailByTuitionId(Long studentCode, Long tuitionId) {
        Tuition tuition = tuitionRepository.findById(tuitionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 고지서가 존재하지 않습니다."));

        if (!tuition.getStudentCode().equals(studentCode)) {
            throw new IllegalArgumentException("본인의 등록금 내역만 상세 조회할 수 있습니다.");
        }

        List<Scholarship> studentScholarships = scholarshipRepository
                .findByStudentCodeAndYearAndSemester(studentCode, tuition.getYear(), tuition.getSemester());

        long calculatedDiscount = studentScholarships.stream().mapToLong(Scholarship::getScholarshipAmount).sum();

        if (tuition.getTotalDiscount() != calculatedDiscount) {
            long finalAmount = Math.max(0, tuition.getBaseAmount() - calculatedDiscount);
            tuition.updateScholarshipDeduction(calculatedDiscount, finalAmount);
        }

        return new TuitionRes.MyTuitionDetailRes(tuition);
    }

    @Transactional
    public void requestTuitionPaymentPending(Long studentCode, Long tuitionId) {
        schedulePeriodValidator.checkTuitionPayment();

        Tuition tuition = tuitionRepository.findById(tuitionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 등록금 고지 내역이 없습니다."));

        if (!tuition.getStudentCode().equals(studentCode)) {
            throw new IllegalArgumentException("본인의 등록금 고지서만 납부 신청이 가능합니다.");
        }

        if (tuition.getStatus() == EnumTuitionStatus.PENDING || tuition.getStatus() == EnumTuitionStatus.PAID) {
            throw new IllegalStateException("이미 신청 완료되었거나 납부가 완료된 상태입니다.");
        }

        tuition.requestPayment();
    }

    // ==========================================
    // [관리자] 서비스 로직
    // ==========================================

    public Page<TuitionRes> getTuitionListForAdmin(Integer year, Integer semester, EnumTuitionStatus status, Pageable pageable) {
        Page<Tuition> tuitionPage = (status != null)
                ? tuitionRepository.findByYearAndSemesterAndStatus(year, semester, status, pageable)
                : tuitionRepository.findByYearAndSemester(year, semester, pageable);

        // 1. 필요한 학생 memberCode 리스트 추출
        List<Long> memberCodes = tuitionPage.getContent().stream()
                .map(Tuition::getStudentCode)
                .collect(Collectors.toList());

        // 2. StudentCache에서 데이터 일괄 조회 (가정: studentCacheRepository 존재)
        Map<Long, StudentCache> studentMap = studentCacheRepository.findAllById(memberCodes).stream()
                .collect(Collectors.toMap(StudentCache::getMemberCode, s -> s));

        // 3. [추가] 필요한 모든 majorId 추출 후 학과 이름 매핑
        Set<Long> majorIds = studentMap.values().stream()
                .map(StudentCache::getMajorId)
                .collect(Collectors.toSet());

        Map<Long, String> majorNameMap = majorRepository.findAllById(majorIds).stream()
                .collect(Collectors.toMap(Major::getMajorId, Major::getName));

        // 4. 결합하여 DTO 생성 (TuitionRes에 majorName을 넘겨줌)
        return tuitionPage.map(t -> {
            StudentCache sc = studentMap.get(t.getStudentCode());
            String majorName = (sc != null) ? majorNameMap.get(sc.getMajorId()) : "학과 정보 없음";
            return new TuitionRes(t, sc, majorName);
        });
    }

    @Transactional
    public void updateTuitionStatus(Long tuitionId, EnumTuitionStatus status, Long adminCode) {
        Tuition tuition = tuitionRepository.findById(tuitionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 등록금 내역이 존재하지 않습니다."));

        if (tuition.getStatus() == EnumTuitionStatus.PAID && status == EnumTuitionStatus.PAID) {
            throw new IllegalStateException("이미 납부 완료(PAID) 처리된 학생입니다.");
        }

        if (status == EnumTuitionStatus.PAID || status == EnumTuitionStatus.PENDING) {
            List<Scholarship> studentScholarships = scholarshipRepository
                    .findByStudentCodeAndYearAndSemester(tuition.getStudentCode(), tuition.getYear(), tuition.getSemester());

            long calculatedDiscount = studentScholarships.stream().mapToLong(Scholarship::getScholarshipAmount).sum();
            long finalAmount = Math.max(0, tuition.getBaseAmount() - calculatedDiscount);

            tuition.updateScholarshipDeduction(calculatedDiscount, finalAmount);
        }

        tuition.updateStatus(status, adminCode);
    }

    public TuitionRes.TuitionRemindRes previewReminderMails(Integer year, Integer semester) {
        List<Tuition> unpaidList = tuitionRepository.findByYearAndSemesterAndStatus(year, semester, EnumTuitionStatus.UNPAID);
        LocalDateTime deadline = unpaidList.isEmpty() ? LocalDateTime.now() : unpaidList.get(0).getDeadline();

        return TuitionRes.TuitionRemindRes.builder()
                .unpaidCount(unpaidList.size())
                .year(year)
                .semester(semester)
                .dueDate(deadline)
                .mailSubject(String.format("[그린대학교] %d년도 %d학기 등록금 미납 안내 고지", year, semester))
                .mailFrom("academic-support@green.ac.kr")
                .mailBodyPreview("<h3>안녕하세요, 그린대학교 학사지원팀입니다.</h3><p>귀하의 등록금이 현재 미납 상태이니 서둘러 미납해 주시길 바랍니다.</p>")
                .build();
    }

    @Transactional
    public void sendReminderEmailToUnpaidStudents(TuitionReq.MailSendRequest request, Long adminCode) {
        List<Tuition> unpaidList = tuitionRepository.findByYearAndSemesterAndStatus(
                request.getYear(), request.getSemester(), EnumTuitionStatus.UNPAID
        );

        for (Tuition tuition : unpaidList) {
            String studentEmail = "student_" + tuition.getStudentCode() + "@green.ac.kr";
            String mailTitle = String.format("[그린대학교] %d년도 %d학기 등록금 미납 안내", tuition.getYear(), tuition.getSemester());
            String mailContent = createTuitionTemplate(tuition);

            boolean isSuccess = true;
            try {
                TuitionMailEvent event = TuitionMailEvent.builder()
                        .studentCode(tuition.getStudentCode())
                        .email(studentEmail)
                        .title(mailTitle)
                        .content(mailContent)
                        .build();

                kafkaTemplate.send("tuition-mail-topic", event);
            } catch (Exception e) {
                isSuccess = false;
            }

            tuitionMailLogRepository.save(TuitionMailLog.builder()
                    .tuition(tuition)
                    .recipientEmail(studentEmail)
                    .isSuccess(isSuccess)
                    .senderCode(adminCode)
                    .build());
        }
    }

    // API-TUI-11: 등록금 정책 마스터 전체 조회 서비스 (프론트에서 연도/학기 선택 조건 없이 마스터만 전송)
    public List<TuitionRes.PolicyRes> getTuitionPolicyList() {
        return tuitionPolicyRepository.findAll().stream()
                .map(TuitionRes.PolicyRes::new)
                .collect(Collectors.toList());
    }

    // API-TUI-12: 등록금 정책 수정 및 변경 내역 스냅샷 이력 자동 적재 인프라 구현
    @Transactional
    public void updateTuitionPolicy(Long policyId, TuitionReq.UpdatePolicyRequest request, Long adminCode) {
        TuitionPolicy policy = tuitionPolicyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 등록금 정책이 존재하지 않습니다."));

        // 1. 현재 활성화된 등록금 납부 일정 조회 및 수정 제어 차단 검증
        ScheduleCache tuitionSchedule = scheduleCacheRepository
                .findByTypeAndIsActiveTrue(EnumScheduleType.TUITION_PAYMENT)
                .orElse(null);

        if (tuitionSchedule != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(tuitionSchedule.getStartDate()) && now.isBefore(tuitionSchedule.getEndDate())) {
                throw new IllegalStateException("현재 등록금 납부 기간 중이므로 정책을 수정할 수 없습니다.");
            }
        }

        // 2. [추가] 변경 전 데이터 JSON 스냅샷 가공 및 History 영속화
        String beforeDataSnapshot = String.format("{\"baseAmount\": %d}", policy.getBaseAmount());

        TuitionPolicyHistory history = TuitionPolicyHistory.builder()
                .tuitionPolicy(policy)
                .changeType(EnumChangeType.UPDATE)
                .beforeData(beforeDataSnapshot)
                .changeReason("관리자 정책 조정")
                .updatorCode(adminCode)
                .build();

        tuitionPolicyHistoryRepository.save(history);

        // 3. 마스터 테이블 갱신 처리
        policy.updateBaseAmount(request.getBaseAmount(), adminCode);
    }

    private String createTuitionTemplate(Tuition tuition) {
        return "<h3>안녕하세요, 그린대학교 학사지원팀입니다.</h3>" +
                "<p>귀하의 " + tuition.getYear() + "년도 " + tuition.getSemester() + "학기 등록금이 현재 <strong>미납</strong> 상태입니다.</p>" +
                "<ul>" +
                "<li><strong>최종 납부 금액:</strong> " + String.format("%,d", tuition.getFinalAmount()) + "원</li>" +
                "</ul>";
    }

    public List<TuitionRes.PolicyHistoryRes> getPolicyHistoryList(Integer year, Integer semester) {
        // 1. 해당 연도의 전체 히스토리를 가져옴 (연도만 기준)
        LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(year, 12, 31, 23, 59, 59);

        return tuitionPolicyHistoryRepository.findByPeriod(start, end)
                .stream()
                .filter(h -> {
                    // 2. 생성월을 기준으로 학기 판별
                    int month = h.getCreatedAt().getMonthValue();
                    int targetSemester = (month <= 6) ? 1 : 2;
                    return targetSemester == semester;
                })
                .map(h -> {
                    Long amount = 0L;
                    try {
                        JsonNode node = objectMapper.readTree(h.getBeforeData());
                        if (node.has("baseAmount")) {
                            amount = node.get("baseAmount").asLong();
                        }
                    } catch (Exception e) {
                        log.error("JSON 파싱 실패", e);
                    }
                    return new TuitionRes.PolicyHistoryRes(h, amount);
                })
                .collect(Collectors.toList());
    }
}