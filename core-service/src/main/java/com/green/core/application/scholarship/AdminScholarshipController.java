package com.green.core.application.scholarship;

import com.green.core.application.scholarship.model.ScholarshipRes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/scholarships")
public class AdminScholarshipController {

    private final ScholarshipService scholarshipService;

    // API-SCH-01: 관리자 - 장학 수혜 학생 목록 조회
    @GetMapping
    public ResponseEntity<Page<ScholarshipRes>> getScholarshipList(
            @RequestParam Integer year,
            @RequestParam Integer semester,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                scholarshipService.getScholarshipList(year, semester, pageable)
        );
    }

}