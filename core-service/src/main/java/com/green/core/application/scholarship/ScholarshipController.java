package com.green.core.application.scholarship;

import com.green.common.auth.MemberContext;
import com.green.core.application.scholarship.model.MyScholarshipListRes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/scholarships")
public class ScholarshipController {

    private final ScholarshipService scholarshipService;

    // API-SCH-02: 학생 - 내 장학금 목록
    @GetMapping("/my")
    public ResponseEntity<Page<MyScholarshipListRes>> getMyScholarships(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long memberCode = MemberContext.get().memberCode();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                scholarshipService.getMyScholarships(memberCode, pageable)
        );
    }

    // 테스트용 수동 실행 엔드포인트 (테스트 후 제거 권장)
//    @PostMapping("/assign")
//    public ResponseEntity<String> assignScholarships(
//            @RequestParam Integer year,
//            @RequestParam Integer semester) {
//        scholarshipService.assignScholarships(year, semester);
//        return ResponseEntity.ok("장학금 배정 완료");
//    }
}