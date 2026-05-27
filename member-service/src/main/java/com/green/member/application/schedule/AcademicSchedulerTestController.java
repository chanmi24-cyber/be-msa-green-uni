package com.green.member.application.schedule;

import com.green.common.model.ResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * [테스트 전용] 학기 갱신 수동 트리거 컨트롤러
 * 테스트 완료 후 삭제할 것
 */
@RestController
@RequestMapping("/test/scheduler")
@RequiredArgsConstructor
public class AcademicSchedulerTestController {

    private final SemesterSchedulerService semesterSchedulerService;

    // POST /test/scheduler/advance-semester
    @PostMapping("/advance-semester")
    public ResultResponse<String> advanceSemester() {
        semesterSchedulerService.advanceSemester();
        return ResultResponse.<String>builder()
                .message("학기 갱신 완료")
                .build();
    }
}
