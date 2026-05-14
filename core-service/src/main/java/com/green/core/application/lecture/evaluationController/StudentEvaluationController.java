package com.green.core.application.lecture.evaluationController;

import com.green.common.auth.MemberContext;
import com.green.common.model.MemberDto;
import com.green.common.model.ResultResponse;
import com.green.core.application.lecture.EvaluationService;
import com.green.core.application.lecture.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/student/evaluations")
public class StudentEvaluationController {
    private final EvaluationService evaluationService;

    @GetMapping
    public ResultResponse<List<EvalListRes>> getStudentEvalList(@ModelAttribute EvalListReq req) {
        MemberDto memberDto = MemberContext.get();
        return ResultResponse.<List<EvalListRes>>builder()
                .message("나의 강의평가 목록 조회 성공")
                .data(evaluationService.getStudentEvalList(memberDto, req))
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
    public ResultResponse<?> createEvaluation(@RequestBody EvalCreateReq req) {
        MemberDto memberDto = MemberContext.get();
        evaluationService.createEvaluation(memberDto, req);
        return ResultResponse.builder()
                .message("강의평가 등록 성공")
                .build();
    }
}