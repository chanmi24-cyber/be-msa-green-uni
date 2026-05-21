package com.green.core.application.grade;

import com.green.common.model.ResultResponse;
import com.green.core.application.grade.model.GradeAppealReq;
import com.green.core.application.grade.model.GradeAppealRes;
import com.green.core.application.grade.model.GradeAppealStuListRes;
import com.green.core.application.grade.model.GradeStudentDetailRes;
import com.green.core.application.grade.model.GradeStudentRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// API-GPA-05: 학생 성적 조회
@RestController
@RequiredArgsConstructor
@RequestMapping("/student/grades")
public class GradeStudentController {

    private final GradeService gradeService;

    @GetMapping("/my")
    public ResponseEntity<ResultResponse<GradeStudentRes>> getStudentGrades(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer semester) {
        return ResponseEntity.ok(
                new ResultResponse<>("성적 조회 성공", gradeService.getStudentGrades(year, semester)));
    }

    // API-GPA-06: 학생 전체 성적 상세조회
    @GetMapping("/my/detail")
    public ResponseEntity<ResultResponse<GradeStudentDetailRes>> getStudentAllGrades() {
        return ResponseEntity.ok(
                new ResultResponse<>("성적 상세 조회 성공", gradeService.getStudentAllGrades()));
    }

    // 학생 이의신청 내역 조회
    @GetMapping("/appeals/my")
    public ResponseEntity<ResultResponse<List<GradeAppealStuListRes>>> getStudentAppealList() {
        return ResponseEntity.ok(
                new ResultResponse<>("이의신청 내역 조회 성공", gradeService.getStudentAppealList()));
    }

    // API-GPA-07: 이의신청 폼 사전 조회
    @GetMapping("/{gradeId}/appeal")
    public ResponseEntity<ResultResponse<GradeAppealRes>> getAppealInfo(
            @PathVariable Long gradeId) {
        return ResponseEntity.ok(
                new ResultResponse<>("이의신청 정보 조회 성공", gradeService.getAppealInfo(gradeId)));
    }

    // API-GPA-07: 이의신청 제출
    @PostMapping("/{gradeId}/appeal")
    public ResponseEntity<ResultResponse<Void>> submitAppeal(
            @PathVariable Long gradeId,
            @RequestBody @Valid GradeAppealReq req) {
        gradeService.submitAppeal(gradeId, req);
        return ResponseEntity.ok(new ResultResponse<>("이의신청이 완료되었습니다.", null));
    }
}