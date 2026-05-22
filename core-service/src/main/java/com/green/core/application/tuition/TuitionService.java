package com.green.core.application.tuition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.common.enumcode.EnumScheduleType;
import com.green.common.enumcode.EnumChangeType;
import com.green.common.exception.BusinessException;
import com.green.core.application.major.MajorRepository;
import com.green.core.application.scholarship.ScholarshipRepository;
import com.green.core.application.tuition.model.TuitionReq;
import com.green.core.application.tuition.model.TuitionRes;
import com.green.core.entity.cache.ScheduleCache;
import com.green.core.entity.cache.StudentCache;
import com.green.core.entity.major.Major;
import com.green.core.entity.scholarship.Scholarship;
import com.green.core.entity.tuition.Tuition;
import com.green.core.entity.tuition.TuitionMailLog;
import com.green.core.entity.tuition.TuitionPolicy;
import com.green.core.entity.tuition.TuitionPolicyHistory;
import com.green.core.enumcode.EnumTuitionStatus;
import com.green.core.repository.ScheduleCacheRepository;
import com.green.core.repository.StudentCacheRepository;
import com.green.core.scheduleValidator.SchedulePeriodValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final TuitionPolicyHistoryRepository tuitionPolicyHistoryRepository;
    private final TuitionMailLogRepository tuitionMailLogRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final SchedulePeriodValidator schedulePeriodValidator;

    // ❌ private final KafkaTemplate<String, Object> kafkaTemplate; // 더 이상 카프카를 거치지 않으므로 제거

    private final ScheduleCacheRepository scheduleCacheRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StudentCacheRepository studentCacheRepository;
    private final MajorRepository majorRepository;

    // 🎯 [주입] 같은 core 모듈에 새로 생성한 EmailSender를 주입합니다.
    private final EmailSender emailSender;

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

        List<Long> memberCodes = tuitionPage.getContent().stream()
                .map(Tuition::getStudentCode)
                .collect(Collectors.toList());

        Map<Long, StudentCache> studentMap = studentCacheRepository.findAllById(memberCodes).stream()
                .collect(Collectors.toMap(StudentCache::getMemberCode, s -> s));

        Set<Long> majorIds = studentMap.values().stream()
                .map(StudentCache::getMajorId)
                .collect(Collectors.toSet());

        Map<Long, String> majorNameMap = majorRepository.findAllById(majorIds).stream()
                .collect(Collectors.toMap(Major::getMajorId, Major::getName));

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

    // 🎯 3단계 핵심 수정: Kafka 발행 로직 제거 후 EmailSender 직접 동기 호출
    @Transactional
    public void sendReminderEmailToUnpaidStudents(TuitionReq.MailSendRequest request, Long adminCode) {
        List<Tuition> unpaidList = tuitionRepository.findByYearAndSemesterAndStatus(
                request.getYear(), request.getSemester(), EnumTuitionStatus.UNPAID
        );

        for (Tuition tuition : unpaidList) {
            String studentEmail = studentCacheRepository.findByMemberCode(tuition.getStudentCode())
                    .map(StudentCache::getEmail)
                    .orElse("student_" + tuition.getStudentCode() + "@green.ac.kr");

            String mailTitle = String.format("[그린대학교] %d년도 %d학기 등록금 미납 안내", tuition.getYear(), tuition.getSemester());
            String mailContent = createTuitionTemplate(tuition);

            boolean isSuccess = true;
            try {
                // 🎯 Kafka로 토픽을 쏘는 대신, 주입받은 emailSender의 메서드를 즉시 동기식으로 호출합니다.
                emailSender.sendRawHtmlMail(studentEmail, mailTitle, mailContent);
                log.info("등록금 미납 메일 발송 성공 - 학생코드: {}, 이메일: {}", tuition.getStudentCode(), studentEmail);
            } catch (Exception e) {
                // SMTP 전송 중 거부되거나 예외 발생 시 캐치하여 로깅 처리 및 결과 반영
                isSuccess = false;
                log.error("등록금 미납 메일 발송 실패 - 학생코드: {}, 사유: {}", tuition.getStudentCode(), e.getMessage());
            }

            // 이제 구글 메일 발송 결과(isSuccess)가 테이블에 완벽하게 정합성을 이루며 적재됩니다.
            tuitionMailLogRepository.save(TuitionMailLog.builder()
                    .tuition(tuition)
                    .recipientEmail(studentEmail)
                    .isSuccess(isSuccess)
                    .senderCode(adminCode)
                    .build());
        }
    }

    public List<TuitionRes.PolicyRes> getTuitionPolicyList() {
        return tuitionPolicyRepository.findAll().stream()
                .map(TuitionRes.PolicyRes::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateTuitionPolicy(Long policyId, TuitionReq.UpdatePolicyRequest request, Long adminCode) {
        TuitionPolicy policy = tuitionPolicyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 등록금 정책이 존재하지 않습니다."));

        if (request.getBaseAmount() == null || request.getBaseAmount() <= 1000000) {
            throw new IllegalStateException("등록금 책정액은 1,000,000원보다 커야 합니다. 금액을 다시 확인해주세요.");
        }

        ScheduleCache tuitionSchedule = scheduleCacheRepository
                .findByTypeAndIsActiveTrue(EnumScheduleType.TUITION_PAYMENT)
                .orElse(null);

        if (tuitionSchedule != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(tuitionSchedule.getStartDate()) && now.isBefore(tuitionSchedule.getEndDate())) {
                throw new IllegalStateException("현재 등록금 납부 기간 중이므로 정책을 수정할 수 없습니다.");
            }
        }

        String beforeDataSnapshot = String.format("{\"baseAmount\": %d}", policy.getBaseAmount());

        TuitionPolicyHistory history = TuitionPolicyHistory.builder()
                .tuitionPolicy(policy)
                .changeType(EnumChangeType.UPDATE)
                .beforeData(beforeDataSnapshot)
                .changeReason("관리자 정책 조정")
                .updatorCode(adminCode)
                .build();

        tuitionPolicyHistoryRepository.save(history);

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
        LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(year, 12, 31, 23, 59, 59);

        return tuitionPolicyHistoryRepository.findByPeriod(start, end)
                .stream()
                .filter(h -> {
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

    public TuitionRes.PaymentPeriodRes getTuitionPaymentPeriod() {
        ScheduleCache schedule = scheduleCacheRepository
                .findByTypeAndIsActiveTrue(EnumScheduleType.TUITION_PAYMENT)
                .orElse(null);

        if (schedule == null) {
            return new TuitionRes.PaymentPeriodRes(false, null, null);
        }

        LocalDateTime now = LocalDateTime.now();
        boolean isPaymentPeriod = now.isAfter(schedule.getStartDate())
                && now.isBefore(schedule.getEndDate());

        return new TuitionRes.PaymentPeriodRes(
                isPaymentPeriod,
                schedule.getStartDate(),
                schedule.getEndDate()
        );
    }
}