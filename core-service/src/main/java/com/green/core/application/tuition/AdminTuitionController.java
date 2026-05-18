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

@RestController
@RequestMapping("/admin/tuitions")
@RequiredArgsConstructor
public class AdminTuitionController {
    private final TuitionService tuitionService;

    // 1. 등록금 납부 목록 조회
    @GetMapping
    public ResponseEntity<Page<TuitionRes>> getTuitionList(
            @RequestParam Integer year,
            @RequestParam Integer semester,
            @RequestParam(required = false) EnumTuitionStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(tuitionService.getTuitionListForAdmin(year, semester, status, pageable));
    }

    // 2. 등록금 납부 상태 변경
    @PatchMapping("/{tuitionId}/status")
    public ResponseEntity<Void> updateTuitionStatus(
            @PathVariable Long tuitionId,
            @RequestBody TuitionReq.UpdateStatusRequest request
    ) {
        Long adminCode = MemberContext.get().memberCode();
        tuitionService.updateTuitionStatus(tuitionId, request.getStatus(), adminCode);
        return ResponseEntity.ok().build();
    }

    // 3. 등록금 미납자 메일 발송
    @PostMapping("/mail/remind")
    public ResponseEntity<Void> sendReminderMails(@RequestBody TuitionReq.MailSendRequest request) {
        Long adminCode = MemberContext.get().memberCode();
        tuitionService.sendReminderEmailToUnpaidStudents(request, adminCode);
        return ResponseEntity.ok().build();
    }

    // 4. 기본 등록금 수정 (정책 테이블 수정)
    @PutMapping("/policies/{policyId}")
    public ResponseEntity<Void> updatePolicy(
            @PathVariable Long policyId,
            @RequestBody TuitionReq.UpdatePolicyRequest request
    ) {
        Long adminCode = MemberContext.get().memberCode();
        tuitionService.updateTuitionPolicy(policyId, request, adminCode);
        return ResponseEntity.ok().build();
    }
}