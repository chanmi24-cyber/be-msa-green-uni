package com.green.core.application.lecture.evaluationController;

import com.green.common.auth.MemberContext;
import com.green.common.model.MemberDto;
import com.green.common.model.ResultResponse;
import com.green.core.application.lecture.EvaluationService;
import com.green.core.application.lecture.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/student/evaluations")
public class StudentEvaluationController {
    private final EvaluationService evaluationService;

    @GetMapping
    public ResultResponse<Page<EvalListRes>> getStudentEvalList(
            @ModelAttribute EvalListReq req,
            @PageableDefault(size = 10) Pageable pageable) {
        MemberDto memberDto = MemberContext.get();
        return ResultResponse.<Page<EvalListRes>>builder()
                .message("나의 강의평가 목록 조회 성공")
                .data(evaluationService.getStudentEvalList(memberDto, req, pageable))
                .build();
    }

    @GetMapping("/years")
    public ResultResponse<List<Integer>> getStudentEvalYears() {
        MemberDto memberDto = MemberContext.get();
        return ResultResponse.<List<Integer>>builder()
                .message("학생 강의평가 연도 목록 조회 성공")
                .data(evaluationService.getStudentEvalYears(memberDto))
                .build();
    }

    @GetMapping("/{lectureId}")
    public ResultResponse<StdEvalDetailRes> getStudentEvalDetail(@PathVariable Long lectureId) {
        MemberDto memberDto = MemberContext.get();
        return ResultResponse.<StdEvalDetailRes>builder()
                .message("강의평가 상세 조회 성공")
                .data(evaluationService.getStudentEvalDetail(memberDto, lectureId))
                .build();
    }

    @PostMapping("/{lectureId}")
    public ResultResponse<?> createEvaluation(@PathVariable Long lectureId, @RequestBody EvalCreateReq req) {
        MemberDto memberDto = MemberContext.get();
        evaluationService.createEvaluation(memberDto, lectureId, req);
        return ResultResponse.builder()
                .message("강의평가 등록 성공")
                .build();
    }


}