package com.green.core.application.tuition;

import com.green.common.auth.MemberContext;
import com.green.core.application.tuition.model.TuitionReq;
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

    // 1. 등록금 전체납부 이력 조회
    @GetMapping
    public ResponseEntity<List<TuitionRes>> getMyTuitionList() {
        Long studentCode = MemberContext.get().memberCode();
        return ResponseEntity.ok(tuitionService.getStudentTuitionList(studentCode));
    }

    // 2. 등록금 납부 상세 조회 (이번 학기 고지서 확인)
    @GetMapping("/detail")
    public ResponseEntity<TuitionRes> getMyTuitionDetail(
            @RequestParam Integer year,
            @RequestParam Integer semester
    ) {
        Long studentCode = MemberContext.get().memberCode();
        return ResponseEntity.ok(tuitionService.getStudentTuitionDetail(studentCode, year, semester));
    }

    // 3. 등록금 납부 신청
    @PostMapping("/payment")
    public ResponseEntity<Void> requestPayment(@RequestBody TuitionReq.PaymentRequest request) {
        Long studentCode = MemberContext.get().memberCode();
        tuitionService.requestTuitionPayment(studentCode, request);
        return ResponseEntity.ok().build();
    }
}