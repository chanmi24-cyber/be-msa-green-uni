package com.green.core.application.tuition;

import com.green.common.auth.MemberContext;
import com.green.core.application.tuition.model.TuitionRes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tuitions")
@RequiredArgsConstructor
public class StudentTuitionController {
    private final TuitionService tuitionService;

    // API-TUI-01: 등록금 납부 내역 전체 조회
    @GetMapping("/my")
    public ResponseEntity<Page<TuitionRes>> getMyTuitionList(
            @PageableDefault(size = 10) Pageable pageable) {
        Long studentCode = MemberContext.get().memberCode();
        return ResponseEntity.ok(tuitionService.getStudentTuitionList(studentCode, pageable));
    }

    // API-TUI-14-STU: 학생용 등록금 납부 기간 조회
    @GetMapping("/payment-period")
    public ResponseEntity<TuitionRes.PaymentPeriodRes> getPaymentPeriod() {
        return ResponseEntity.ok(tuitionService.getTuitionPaymentPeriod());
    }

    // API-TUI-03: 등록금 납부 상세 조회
    @GetMapping("/detail/{tuitionId}")
    public ResponseEntity<TuitionRes.MyTuitionDetailRes> getMyTuitionDetail(@PathVariable Long tuitionId) {
        Long studentCode = MemberContext.get().memberCode();
        return ResponseEntity.ok(tuitionService.getStudentTuitionDetailByTuitionId(studentCode, tuitionId));
    }

    // API-TUI-07: 학생 납부 신청
    @PatchMapping("/{tuitionId}/pending")
    public ResponseEntity<Void> requestPayment(@PathVariable Long tuitionId) {
        Long studentCode = MemberContext.get().memberCode();
        tuitionService.requestTuitionPaymentPending(studentCode, tuitionId);
        return ResponseEntity.ok().build();
    }
}