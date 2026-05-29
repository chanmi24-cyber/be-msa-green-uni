package com.green.core.application.grade;

import com.green.common.model.ResultResponse;
import com.green.core.application.grade.model.GradeAppealProDetailRes;
import com.green.core.application.grade.model.GradeAppealProListRes;
import com.green.core.application.grade.model.GradeAppealProReq;
import com.green.core.application.grade.model.GradeLectureListRes;
import com.green.core.application.grade.model.GradeListRes;
import com.green.core.application.grade.model.GradeUpdateReq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

// [수정] 잘못된 /api/calendars 경로 → 교수 성적 API로 교체
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/professor/grades")
public class GradeController {

    private final GradeService gradeService;

    // 교수 담당 강의 목록 (성적 관리 강의 선택 화면용)
    // [추가] GET /professor/grades/lectures
    @GetMapping("/lectures")
    public ResponseEntity<ResultResponse<List<GradeLectureListRes>>> getProfessorLectures() {
        return ResponseEntity.ok(new ResultResponse<>("강의 목록 조회 성공", gradeService.getProfessorLectures()));
    }

    // API-GPA-03: 교수 성적 조회
    @GetMapping("/{lectureId}")
    public ResponseEntity<ResultResponse<List<GradeListRes>>> getProfessorGrades(
            @PathVariable Long lectureId) {
        return ResponseEntity.ok(new ResultResponse<>("성적 조회 성공", gradeService.getProfessorGrades(lectureId)));
    }

    // API-GPA-02: 교수 성적 입력/수정
    @PatchMapping("/{lectureId}")
    public ResponseEntity<ResultResponse<Void>> updateGrades(
            @PathVariable Long lectureId,
            @RequestBody List<GradeUpdateReq> reqList) {
        gradeService.updateGrades(lectureId, reqList);
        return ResponseEntity.ok(new ResultResponse<>("성적 저장 성공", null));
    }

    // 교수 이의신청 목록 조회
    @GetMapping("/appeals")
    public ResponseEntity<ResultResponse<Page<GradeAppealProListRes>>> getProfessorAppealList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(new ResultResponse<>("이의신청 목록 조회 성공",
                gradeService.getProfessorAppealList(PageRequest.of(page - 1, size))));
    }

    // 교수 이의신청 상세 조회
    @GetMapping("/appeals/{courseId}")
    public ResponseEntity<ResultResponse<GradeAppealProDetailRes>> getProfessorAppealDetail(
            @PathVariable Long courseId) {
        return ResponseEntity.ok(new ResultResponse<>("이의신청 상세 조회 성공",
                gradeService.getProfessorAppealDetail(courseId)));
    }

    // 교수 이의신청 처리 (승인/반려)
    @PatchMapping("/appeals/{courseId}")
    public ResponseEntity<ResultResponse<Void>> processAppeal(
            @PathVariable Long courseId,
            @RequestBody @Valid GradeAppealProReq req) {
        gradeService.processAppeal(courseId, req);
        return ResponseEntity.ok(new ResultResponse<>("이의신청 처리 완료", null));
    }
}