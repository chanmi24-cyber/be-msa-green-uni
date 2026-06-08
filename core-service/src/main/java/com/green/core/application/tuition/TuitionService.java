package com.green.core.application.tuition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.common.enumcode.EnumScheduleType;
import com.green.common.enumcode.EnumChangeType;
import com.green.common.kafka.TuitionPaidEvent;
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
    private final TuitionPolicyHistoryRepository tuitionPolicyHistoryRepository;
    private final TuitionMailLogRepository tuitionMailLogRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final SchedulePeriodValidator schedulePeriodValidator;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TuitionMailAsyncService tuitionMailAsyncService;

    // ❌ private final KafkaTemplate<String, Object> kafkaTemplate; // 더 이상 카프카를 거치지 않으므로 제거

    private final ScheduleCacheRepository scheduleCacheRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StudentCacheRepository studentCacheRepository;
    private final MajorRepository majorRepository;

    // 🎯 [주입] 같은 core 모듈에 새로 생성한 EmailSender를 주입합니다.
    private final EmailSender emailSender;

    @Transactional
    public void syncTuitionAmountWithCurrentMajor(Long studentCode, Integer year, Integer semester) {
        Tuition tuition = tuitionRepository.findByStudentCodeAndYearAndSemester(studentCode, year, semester)
                .orElseThrow(() -> new IllegalArgumentException("해당 학기의 등록금 고지 내역이 존재하지 않습니다."));

        // 💡 핵심 방어벽: 이미 납부한 과거 학기는 절대로 건드리지 않음!
        if (tuition.getStatus() == EnumTuitionStatus.PAID) {
            log.info("[전과 반영 스킵] 학생 {}의 {}-{} 학기 등록금은 이미 납부 완료되어 스냅샷을 유지합니다.", studentCode, year, semester);
            return;
        }

        StudentCache student = studentCacheRepository.findByMemberCode(studentCode)
                .orElseThrow(() -> new IllegalArgumentException("학생 정보가 캐시에 존재하지 않습니다."));

        Major major = majorRepository.findById(student.getMajorId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학과 ID입니다."));

        TuitionPolicy policy = tuitionPolicyRepository.findByCollegeCollegeId(major.getCollege().getCollegeId())
                .orElseThrow(() -> new IllegalArgumentException(major.getCollege().getName() + "의 등록금 정책이 설정되지 않았습니다."));

        Long newBaseAmount = policy.getBaseAmount();
        List<Scholarship> studentScholarships = scholarshipRepository.findByStudentCodeAndYearAndSemester(studentCode, year, semester);
        long calculatedDiscount = studentScholarships.stream().mapToLong(Scholarship::getScholarshipAmount).sum();
        long newFinalAmount = Math.max(0, newBaseAmount - calculatedDiscount);

        // 💡 변경된 majorId(student.getMajorId())를 함께 넘겨 고지서의 학과를 경영학과로 새로 고칩니다.
        tuition.updatePolicyAndBaseAmount(policy, newBaseAmount, newFinalAmount, student.getMajorId());
        tuition.updateScholarshipDeduction(calculatedDiscount, newFinalAmount);
    }

    @Transactional
    public void handleStudentMajorChangedEvent(Long studentCode) {
        // 현재 시점 기준으로 동기화가 필요한 타겟 학기 산출 (현재 날짜 기준)
        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear();
        int currentSemester = (now.getMonthValue() <= 6) ? 1 : 2;

        try {
            syncTuitionAmountWithCurrentMajor(studentCode, currentYear, currentSemester);
        } catch (Exception e) {
            // 학기 전이라 고지서가 아직 발급 안 되었거나, 예외 케이스 발생 시 로그 처리 후 스킵
            log.warn("[전과 반영 스킵] 학생코드: {} - 전과 등록금 재계산이 완료되지 않았습니다. 사유: {}",
                    studentCode, e.getMessage());
        }
    }

    // ==========================================
    // [학생] 서비스 로직
    // ==========================================

    public Page<TuitionRes> getStudentTuitionList(Long studentCode, Pageable pageable) {
        return tuitionRepository.findByStudentCodeOrderByYearDescSemesterDesc(studentCode, pageable)
                .map(TuitionRes::new);
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

    public Page<TuitionRes> getTuitionListForAdmin(Integer year, Integer semester, EnumTuitionStatus status, String searchKeyword, Pageable pageable) { // 💡 1. searchKeyword 파라미터 추가
        Long searchStudentCode = null;
        List<Long> searchedStudentCodes = null;

        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            String keyword = searchKeyword.trim();

            if (keyword.matches("\\d+")) {
                // 1) 숫자인 경우 -> 학번 단건 검색
                searchStudentCode = Long.parseLong(keyword);
            } else {
                // 2) 문자인 경우 -> 이름 포함 검색
                List<StudentCache> students = studentCacheRepository.findByNameContaining(keyword);

                if (!students.isEmpty()) {
                    searchedStudentCodes = students.stream()
                            .map(StudentCache::getMemberCode)
                            .collect(Collectors.toList());
                } else {
                    // 검색어에 매칭되는 학생이 캐시에 없으면 즉시 빈 페이지를 리턴합니다.
                    return Page.empty(pageable);
                }
            }
        }

        // 💡 2. 데이터베이스 조건 검색 실행 (중복 덮어쓰기 코드 제거 완료)
        Page<Tuition> tuitionPage = tuitionRepository.findTuitionWithFilters(
                year, semester, status, searchStudentCode, searchedStudentCodes, pageable
        );

        // 3. 페이징된 결과 데이터를 기반으로 화면에 뿌려줄 학생 및 학과 정보 조립
        List<Long> memberCodes = tuitionPage.getContent().stream()
                .map(Tuition::getStudentCode)
                .collect(Collectors.toList());

        if (memberCodes.isEmpty()) {
            return Page.empty(pageable);
        }

        Map<Long, StudentCache> studentMap = studentCacheRepository.findAllById(memberCodes).stream()
                .collect(Collectors.toMap(StudentCache::getMemberCode, s -> s));

        Set<Long> majorIds = tuitionPage.getContent().stream()
                .map(Tuition::getMajorId)
                .collect(Collectors.toSet());

        Map<Long, String> majorNameMap = majorRepository.findAllById(majorIds).stream()
                .collect(Collectors.toMap(Major::getMajorId, Major::getName));

        return tuitionPage.map(t -> {
            StudentCache sc = studentMap.get(t.getStudentCode());
            // 💡 학생 캐시의 전공이 아니라, 고지서 엔티티(t)가 들고 있는 당해 학기 majorId로 이름을 찾음!
            String majorName = majorNameMap.getOrDefault(t.getMajorId(), "학과 정보 없음");
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

        // 1. 등록금 상태를 PAID 등으로 변경
        tuition.updateStatus(status, adminCode);

        // 🎯 2. 만약 변경된 상태가 'PAID(납부완료)'라면 Kafka 이벤트 발행!
        if (status == EnumTuitionStatus.PAID) {
            TuitionPaidEvent event = new TuitionPaidEvent(
                    tuition.getStudentCode(),
                    tuition.getYear(),
                    tuition.getSemester()
            );

            kafkaTemplate.send("tuition-paid-topic", event);
            log.info("[Kafka 발행] 등록금 납부 완료 이벤트 전송 - 학생코드: {}, 학기: {}-{}",
                    tuition.getStudentCode(), tuition.getYear(), tuition.getSemester());
        }
    }

    public TuitionRes.TuitionRemindRes previewReminderMails(Integer year, Integer semester) {
        List<Tuition> unpaidList = tuitionRepository.findByYearAndSemesterAndStatus(year, semester, EnumTuitionStatus.UNPAID);

        return TuitionRes.TuitionRemindRes.builder()
                .unpaidCount(unpaidList.size())
                .year(year)
                .semester(semester)
                .mailSubject(String.format("[그린대학교] %d년도 %d학기 등록금 미납 안내 고지", year, semester))
                .mailFrom("academic-support@green.ac.kr")
                .mailBodyPreview("<h3>안녕하세요, 그린대학교 학사지원팀입니다.</h3><p>귀하의 등록금이 현재 미납 상태이니 서둘러 미납해 주시길 바랍니다.</p>")
                .build();
    }

    // 🎯 비동기로 변경된 메일 발송 로직
    @Transactional(readOnly = true) // 💡 대량 조회가 주 목적이므로 readOnly=true로 변경하여 성능 최적화
    public void sendReminderEmailToUnpaidStudents(TuitionReq.MailSendRequest request, Long adminCode) {
        List<Tuition> unpaidList = tuitionRepository.findByYearAndSemesterAndStatus(
                request.getYear(), request.getSemester(), EnumTuitionStatus.UNPAID
        );

        log.info("[메일 발송 시작] 총 {}명의 미납자에게 비동기 메일 발송을 시작합니다.", unpaidList.size());

        for (Tuition tuition : unpaidList) {
            String studentEmail = studentCacheRepository.findByMemberCode(tuition.getStudentCode())
                    .map(StudentCache::getEmail)
                    .orElse("student_" + tuition.getStudentCode() + "@green.ac.kr");

            String mailTitle = String.format("[그린대학교] %d년도 %d학기 등록금 미납 안내", tuition.getYear(), tuition.getSemester());
            String mailContent = createTuitionTemplate(tuition);

            // 🎯 핵심: 주입받은 비동기 서비스의 메서드를 호출합니다.
            // 이 호출은 0.001초만에 지나가며, 실제 작업은 별도 스레드 풀에서 돌아갑니다.
            tuitionMailAsyncService.sendEmailAndLog(tuition, studentEmail, mailTitle, mailContent, adminCode);
        }

        log.info("[메일 발송 요청 완료] 모든 메일 발송 요청이 큐에 등록되었습니다. (관리자 응답 즉시 반환)");
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

        List<ScheduleCache> tuitionScheduleList = scheduleCacheRepository
                .findByTypeAndIsActiveTrue(EnumScheduleType.TUITION_PAYMENT);
        ScheduleCache tuitionSchedule = tuitionScheduleList.isEmpty() ? null : tuitionScheduleList.get(0);

        if (tuitionSchedule != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(tuitionSchedule.getStartDate()) && now.isBefore(tuitionSchedule.getEndDate())) {
                throw new IllegalStateException("현재 등록금 납부 기간 중이므로 정책을 수정할 수 없습니다.");
            }
        }

        // 🎯 [추가] 이력이 어느 학기에 귀속되는지 현재 날짜 기준으로 계산
        LocalDateTime now = LocalDateTime.now();
        int targetYear = now.getYear();
        int targetSemester = (now.getMonthValue() <= 6) ? 1 : 2;

        String beforeDataSnapshot = String.format("{\"baseAmount\": %d}", policy.getBaseAmount());

        // 🎯 [수정] 빌더 패턴에 targetYear와 targetSemester를 명시적으로 주입합니다.
        TuitionPolicyHistory history = TuitionPolicyHistory.builder()
                .tuitionPolicy(policy)
                .changeType(EnumChangeType.UPDATE)
                .beforeData(beforeDataSnapshot)
                .changeReason("관리자 정책 조정")
                .updatorCode(adminCode)
                .targetYear(targetYear)         // ◀ 추가
                .targetSemester(targetSemester) // ◀ 추가
                .build();

        tuitionPolicyHistoryRepository.save(history);

        policy.updateBaseAmount(request.getBaseAmount(), adminCode);
    }

    private String createTuitionTemplate(Tuition tuition) {
        return "<h3>안녕하세요, 그린대학교 학사지원팀입니다.</h3>" +
                "<p>아직 <strong>" + tuition.getYear() + "년 " + tuition.getSemester() + "학기 등록금</strong>이 납부되지 않았습니다.</p>" +
                "<p>납부기한까지 미납 시 수강이 취소될 수 있으니 빠른 시일 내에 납부해 주시기 바랍니다.</p>" + "<br>" +
                "<p>납부 문의: green.uni502@gmail.com</p>" ;
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
        List<ScheduleCache> scheduleList = scheduleCacheRepository
                .findByTypeAndIsActiveTrue(EnumScheduleType.TUITION_PAYMENT);
        ScheduleCache schedule = scheduleList.isEmpty() ? null : scheduleList.get(0);

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