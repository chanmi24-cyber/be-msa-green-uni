package com.green.core.application.tuition;

import com.green.common.auth.MemberContext;
import com.green.core.application.tuition.model.TuitionReq;
import com.green.core.application.tuition.model.TuitionRes;
import com.green.core.enumcode.EnumTuitionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminTuitionController {
    private final TuitionService tuitionService;

    // API-TUI-02: 등록금 납부 학생 목록 조회
    @GetMapping("/tuitions")
    public ResponseEntity<Page<TuitionRes>> getTuitionList(
            @RequestParam Integer year,
            @RequestParam Integer semester,
            @RequestParam(required = false) EnumTuitionStatus status,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(tuitionService.getTuitionListForAdmin(year, semester, status, pageable));
    }

    // API-TUI-05: 등록금 미납자 독촉 메일 미리보기
    @GetMapping("/tuitions/reminders/preview")
    public ResponseEntity<TuitionRes.TuitionRemindRes> previewReminders(
            @RequestParam Integer year,
            @RequestParam Integer semester
    ) {
        return ResponseEntity.ok(tuitionService.previewReminderMails(year, semester));
    }

    // API-TUI-04: 등록금 미납자 독촉 메일 발송
    @PostMapping("/tuitions/reminders")
    public ResponseEntity<Void> sendReminderMails(@RequestBody TuitionReq.MailSendRequest request) {
        Long adminCode = MemberContext.get().memberCode();
        tuitionService.sendReminderEmailToUnpaidStudents(request, adminCode);
        return ResponseEntity.ok().build();
    }

    // API-TUI-08: 관리자 납부 상태 변경 (명세서 명시 주소 규칙 /{tuitionId}/paid 반영)
    @PatchMapping("/tuitions/{tuitionId}/paid")
    public ResponseEntity<Void> updateTuitionStatus(
            @PathVariable Long tuitionId,
            @RequestBody TuitionReq.UpdateStatusRequest request
    ) {
        Long adminCode = MemberContext.get().memberCode();
        tuitionService.updateTuitionStatus(tuitionId, request.getStatus(), adminCode);
        return ResponseEntity.ok().build();
    }

    // API-TUI-11: 등록금 정책 전체 조회
    @GetMapping("/tuition-policies")
    public ResponseEntity<List<TuitionRes.PolicyRes>> getPolicies() {
        return ResponseEntity.ok(tuitionService.getTuitionPolicyList());
    }

    // API-TUI-12: 등록금 정책 수정 (PATCH 매핑 및 자원명 일치)
    @PatchMapping("/tuition-policies/{policyId}")
    public ResponseEntity<Void> updatePolicy(
            @PathVariable Long policyId,
            @RequestBody TuitionReq.UpdatePolicyRequest request
    ) {
        Long adminCode = MemberContext.get().memberCode();
        tuitionService.updateTuitionPolicy(policyId, request, adminCode);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tuition-policies/history")
    public ResponseEntity<List<TuitionRes.PolicyHistoryRes>> getPolicyHistories(
            @RequestParam Integer year,
            @RequestParam Integer semester
    ) {
        return ResponseEntity.ok(tuitionService.getPolicyHistoryList(year, semester));
    }

    // API-TUI-14: 등록금 납부 기간 조회
    @GetMapping("/tuition-schedule/payment-period")
    public ResponseEntity<TuitionRes.PaymentPeriodRes> getPaymentPeriod() {
        return ResponseEntity.ok(tuitionService.getTuitionPaymentPeriod());
    }
}