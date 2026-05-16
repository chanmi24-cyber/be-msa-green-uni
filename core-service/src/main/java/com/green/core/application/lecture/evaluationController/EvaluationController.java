package com.green.core.application.lecture.evaluationController;

import com.green.common.model.ResultResponse;
import com.green.core.application.lecture.EvaluationService;
import com.green.core.application.lecture.model.EvalPeriodReq;
import com.green.core.application.lecture.model.EvalPeriodRes;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/evaluations")
public class EvaluationController {
    private final EvaluationService evaluationService;

    @GetMapping("/periods")
    public ResultResponse<EvalPeriodRes> getEvalPeriod(@ModelAttribute EvalPeriodReq req) {
        return ResultResponse.<EvalPeriodRes>builder()
                .message("강의평가 기간 조회 성공")
                .data(evaluationService.getEvalPeriod(req))
                .build();
    }
}