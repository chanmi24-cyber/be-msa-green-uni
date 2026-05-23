package com.green.core.application.tuition;

import com.green.common.auth.MemberContext;
import com.green.core.application.tuition.model.TuitionRes;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<List<TuitionRes>> getMyTuitionList() {
        Long studentCode = MemberContext.get().memberCode();
        return ResponseEntity.ok(tuitionService.getStudentTuitionList(studentCode));
    }

    // API-TUI-03: 등록금 납부 상세 조회 (Path 변수로 tuitionId 수신 구조 매핑)
    @GetMapping("/{tuitionId}")
    public ResponseEntity<TuitionRes.MyTuitionDetailRes> getMyTuitionDetail(@PathVariable Long tuitionId) {
        Long studentCode = MemberContext.get().memberCode();
        return ResponseEntity.ok(tuitionService.getStudentTuitionDetailByTuitionId(studentCode, tuitionId));
    }

    // API-TUI-07: 학생 납부 신청 (PATCH 매핑 및 pending 엔드포인트 주소 일치)
    @PatchMapping("/{tuitionId}/pending")
    public ResponseEntity<Void> requestPayment(@PathVariable Long tuitionId) {
        Long studentCode = MemberContext.get().memberCode();
        tuitionService.requestTuitionPaymentPending(studentCode, tuitionId);
        return ResponseEntity.ok().build();
    }
}