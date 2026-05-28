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
@RequestMapping("/professor/evaluations")
public class ProfessorEvaluationController {
    private final EvaluationService evaluationService;

    @GetMapping
    public ResultResponse<List<EvalListRes>> getProfessorEvalList(@ModelAttribute EvalListReq req) {
        MemberDto memberDto = MemberContext.get();
        return ResultResponse.<List<EvalListRes>>builder()
                .message("강의평가 목록 조회 성공")
                .data(evaluationService.getProfessorEvalList(memberDto, req))
                .build();
    }

    @GetMapping("/years")
    public ResultResponse<List<Integer>> getProfessorEvalYears() {
        MemberDto memberDto = MemberContext.get();
        return ResultResponse.<List<Integer>>builder()
                .message("교수 강의평가 연도 목록 조회 성공")
                .data(evaluationService.getProfessorEvalYears(memberDto))
                .build();
    }

    @GetMapping("/{lectureId}")
    public ResultResponse<ProEvalDetailRes> getProfessorEvalDetail(@PathVariable Long lectureId) {
        MemberDto memberDto = MemberContext.get();
        return ResultResponse.<ProEvalDetailRes>builder()
                .message("강의평가 상세 조회 성공")
                .data(evaluationService.getProfessorEvalDetail(memberDto, lectureId))
                .build();
    }


}