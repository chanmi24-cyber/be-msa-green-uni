package com.green.core.application.tuition;

import com.green.common.exception.BusinessException;
import com.green.core.application.scholarship.ScholarshipRepository;
import com.green.core.application.tuition.model.TuitionReq;
import com.green.core.application.tuition.model.TuitionRes;
import com.green.core.application.tuition.model.TuitionMailEvent;
import com.green.core.entity.scholarship.Scholarship;
import com.green.core.entity.tuition.Tuition;
import com.green.core.entity.tuition.TuitionMailLog;
import com.green.core.entity.tuition.TuitionPolicy;
import com.green.core.enumcode.EnumTuitionStatus;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TuitionService {

    private final TuitionRepository tuitionRepository;
    private final TuitionPolicyRepository tuitionPolicyRepository;
    private final TuitionMailLogRepository tuitionMailLogRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final SchedulePeriodValidator schedulePeriodValidator;
    private final KafkaTemplate<String, Object> kafkaTemplate;

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

        // 명세서 조건: 본인 외 타인 내역 조회 시도 제한 (403 대응)
        if (!tuition.getStudentCode().equals(studentCode)) {
            throw new IllegalArgumentException("본인의 등록금 내역만 상세 조회할 수 있습니다.");
        }

        // 장학금 수혜 동적 업데이트 연동
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
        schedulePeriodValidator.checkTuitionPayment(); // 기간 검증

        Tuition tuition = tuitionRepository.findById(tuitionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 등록금 고지 내역이 없습니다."));

        // 권한 예외 검증
        if (!tuition.getStudentCode().equals(studentCode)) {
            throw new IllegalArgumentException("본인의 등록금 고지서만 납부 신청이 가능합니다.");
        }

        // 명세서 조건: 이미 PENDING 이거나 PAID 상태일 때 예외 분기 (409 대응)
        if (tuition.getStatus() == EnumTuitionStatus.PENDING || tuition.getStatus() == EnumTuitionStatus.PAID) {
            throw new IllegalStateException("이미 신청 완료되었거나 납부가 완료된 상태입니다.");
        }

        tuition.requestPayment();
    }

    // ==========================================
    // [관리자] 서비스 로직
    // ==========================================

    public Page<TuitionRes> getTuitionListForAdmin(Integer year, Integer semester, EnumTuitionStatus status, Pageable pageable) {
        Page<Tuition> page;
        if (status != null) {
            page = tuitionRepository.findByYearAndSemesterAndStatus(year, semester, status, pageable);
        } else {
            page = tuitionRepository.findByYearAndSemester(year, semester, pageable);
        }
        return page.map(TuitionRes::new);
    }

    @Transactional
    public void updateTuitionStatus(Long tuitionId, EnumTuitionStatus status, Long adminCode) {
        Tuition tuition = tuitionRepository.findById(tuitionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 등록금 내역이 존재하지 않습니다."));

        // 명세서 조건: 이미 PAID 상태인데 또 PAID 하려고 할 때 예외 분기 (409 대응)
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

    // API-TUI-05: 미납자 독촉 메일 미리보기 서비스
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
                .mailBodyPreview("<h3>안녕하세요, 그린대학교 학사지원팀입니다.</h3><p>귀하의 등록금이 미납 상태...</p>")
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

    // API-TUI-11: 등록금 정책 전체 조회 서비스
    public List<TuitionRes.PolicyRes> getTuitionPolicyList() {
        return tuitionPolicyRepository.findAll().stream()
                .map(TuitionRes.PolicyRes::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateTuitionPolicy(Long policyId, TuitionReq.UpdatePolicyRequest request, Long adminCode) {
        TuitionPolicy policy = tuitionPolicyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 등록금 정책이 존재하지 않습니다."));

        // 명세서 조건: 이미 납부 마감 기한이 지난 과거의 정책이거나 수정 불가능 시점 조건 제약 (403 대응)
        if (LocalDateTime.now().isAfter(LocalDateTime.of(policy.getYear(), 2, 28, 18, 0))) {
            throw new IllegalStateException("이미 등록금 고지 및 수납 기한이 만료되어 수정할 수 없습니다.");
        }

        policy.updateBaseAmount(request.getBaseAmount(), adminCode);
    }

    private String createTuitionTemplate(Tuition tuition) {
        return "<h3>안녕하세요, 그린대학교 학사지원팀입니다.</h3>" +
                "<p>귀하의 " + tuition.getYear() + "년도 " + tuition.getSemester() + "학기 등록금이 현재 <strong>미납</strong> 상태입니다.</p>" +
                "<ul>" +
                "<li><strong>최종 납부 금액:</strong> " + String.format("%,d", tuition.getFinalAmount()) + "원</li>" +
                "</ul>";
    }
}