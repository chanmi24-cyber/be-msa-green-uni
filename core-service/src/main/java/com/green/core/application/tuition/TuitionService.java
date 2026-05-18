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
    private final ScholarshipRepository scholarshipRepository; // 2. 장학금 레포지토리 정상 주입
    private final SchedulePeriodValidator schedulePeriodValidator;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // ==========================================
    // [학생] 서비스 로직
    // ==========================================

    /**
     * 1. 등록금 전체납부 조회
     */
    public List<TuitionRes> getStudentTuitionList(Long studentCode) {
        return tuitionRepository.findByStudentCodeOrderByYearDescSemesterDesc(studentCode).stream()
                .map(TuitionRes::new)
                .collect(Collectors.toList());
    }

    /**
     * 2. 등록금 납부 상세 조회 (장학금 여부에 따라 유연하게 실시간 차감 연산)
     */
    @Transactional
    public TuitionRes getStudentTuitionDetail(Long studentCode, Integer year, Integer semester) {
        // 해당 학기 기본 고지서 내역 조회
        Tuition tuition = tuitionRepository.findByStudentCodeAndYearAndSemester(studentCode, year, semester)
                .orElseThrow(() -> new IllegalArgumentException("해당 학기의 등록금 정보가 존재하지 않습니다."));

        // [수정] DB에서 이 학기에 수혜받은 장학금 목록(다자녀, 보훈 등)을 다이렉트로 조회
        List<Scholarship> studentScholarships = scholarshipRepository
                .findByStudentCodeAndYearAndSemester(studentCode, year, semester);

        // 여러 개의 장학 금액을 전부 더함 (예: 30만 + 30만 = 60만)
        long calculatedDiscount = studentScholarships.stream()
                .mapToLong(Scholarship::getScholarshipAmount)
                .sum();

        // 장학 금액 변동(또는 신규 반영)이 있다면 엔티티 상태를 실시간 업데이트하여 유연하게 계산
        if (tuition.getTotalDiscount() != calculatedDiscount) {
            long finalAmount = Math.max(0, tuition.getBaseAmount() - calculatedDiscount);
            tuition.updateScholarshipDeduction(calculatedDiscount, finalAmount);
        }

        return new TuitionRes(tuition);
    }

    /**
     * 3. 등록금 납부 신청
     */
    @Transactional
    public void requestTuitionPayment(Long studentCode, TuitionReq.PaymentRequest request) {
        // [기간 검증] 등록금 납부 기간인지 체크 (SchedulePeriodValidator 활용)
        schedulePeriodValidator.checkTuitionPayment();

        Tuition tuition = tuitionRepository.findByStudentCodeAndYearAndSemester(studentCode, request.getYear(), request.getSemester())
                .orElseThrow(() -> new IllegalArgumentException("등록금 고지 내역이 없습니다."));

        // 가상계좌 발송 처리를 위해 상태를 PENDING(처리중)으로 변경하는 엔티티 비즈니스 메서드 호출
        tuition.requestPayment();
    }

    // ==========================================
    // [관리자] 서비스 로직
    // ==========================================

    /**
     * 1. 등록금 납부 목록 조회 (페이징 및 상태 검색)
     */
    public Page<TuitionRes> getTuitionListForAdmin(Integer year, Integer semester, EnumTuitionStatus status, Pageable pageable) {
        Page<Tuition> page;
        if (status != null) {
            page = tuitionRepository.findByYearAndSemesterAndStatus(year, semester, status, pageable);
        } else {
            page = tuitionRepository.findByYearAndSemester(year, semester, pageable);
        }
        return page.map(TuitionRes::new);
    }

    /**
     * 2. 등록금 납부 상태 변경
     */
    @Transactional
    public void updateTuitionStatus(Long tuitionId, EnumTuitionStatus status, Long adminCode) {
        Tuition tuition = tuitionRepository.findById(tuitionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 등록금 내역이 존재하지 않습니다."));

        // [비즈니스 로직 추가] 만약 상태를 'PAID(납부완료)'나 'PENDING(처리중)' 등으로 변경하려고 할 때,
        // 혹은 정산이 필요한 상태라면 실시간으로 장학금을 계산해서 고지서 금액을 먼저 업데이트해줍니다.
        if (status == EnumTuitionStatus.PAID || status == EnumTuitionStatus.PENDING) {

            // 이 학생이 해당 학기에 수혜받은 장학금 목록 조회
            List<Scholarship> studentScholarships = scholarshipRepository
                    .findByStudentCodeAndYearAndSemester(tuition.getStudentCode(), tuition.getYear(), tuition.getSemester());

            // 총 장학 금액 합산 (ex: 보훈 30만 + 다자녀 30만 = 60만)
            long calculatedDiscount = studentScholarships.stream()
                    .mapToLong(Scholarship::getScholarshipAmount)
                    .sum();

            // 최종 납부 금액 계산 (기본금액 - 장학금, 0원 미만 방지)
            long finalAmount = Math.max(0, tuition.getBaseAmount() - calculatedDiscount);

            // 엔티티의 장학금 할인금액과 최종금액을 정산 업데이트
            tuition.updateScholarshipDeduction(calculatedDiscount, finalAmount);
        }

        // 엔티티 내부 도메인 로직을 통해 상태(status) 변경 및 수정자 사번, 납부시간(PAID인 경우) 적재
        tuition.updateStatus(status, adminCode);
    }

    /**
     * 3. 등록금 미납자 메일 발송 (Kafka 이벤트 발행)
     */
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
                log.info("등록금 미납 메일 발송 이벤트 발행 성공 - 학생코드: {}", tuition.getStudentCode());
            } catch (Exception e) {
                isSuccess = false;
                log.error("등록금 미납 메일 이벤트 발행 실패 - 학생코드: {}, 에러: {}", tuition.getStudentCode(), e.getMessage());
            }

            TuitionMailLog mailLog = TuitionMailLog.builder()
                    .tuition(tuition)
                    .recipientEmail(studentEmail)
                    .isSuccess(isSuccess)
                    .senderCode(adminCode)
                    .build();

            tuitionMailLogRepository.save(mailLog);
        }
    }

    /**
     * 4. 기본 등록금 수정 (등록금 정책 테이블 수정)
     */
    @Transactional
    public void updateTuitionPolicy(Long policyId, TuitionReq.UpdatePolicyRequest request, Long adminCode) {
        TuitionPolicy policy = tuitionPolicyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 등록금 정책이 존재하지 않습니다."));

        // 정책 엔티티 비즈니스 로직 호출
        policy.updateBaseAmount(request.getBaseAmount(), adminCode);
    }

    /**
     * 안내 메일 HTML 템플릿 생성 유틸 메서드
     */
    private String createTuitionTemplate(Tuition tuition) {
        return "<h3>안녕하세요, 그린대학교 학사지원팀입니다.</h3>" +
                "<p>귀하의 " + tuition.getYear() + "년도 " + tuition.getSemester() + "학기 등록금이 현재 <strong>미납</strong> 상태입니다.</p>" +
                "<ul>" +
                "<li><strong>기본 등록금:</strong> " + String.format("%,d", tuition.getBaseAmount()) + "원</li>" +
                "<li><strong>장학 할인 금액:</strong> " + String.format("%,d", tuition.getTotalDiscount()) + "원</li>" +
                "<li><strong>최종 납부 금액:</strong> <span style='color:red;'>" + String.format("%,d", tuition.getFinalAmount()) + "원</span></li>" +
                "<li><strong>납부 마감 기한:</strong> " + tuition.getDeadline().toString().replace("T", " ") + "까지</li>" +
                "</ul>" +
                "<p>기한 내에 등록금을 납부하지 않을 경우 학칙에 따라 미등록 제적 등 불이익을 받을 수 있으니 유의하시기 바랍니다.</p>";
    }
}