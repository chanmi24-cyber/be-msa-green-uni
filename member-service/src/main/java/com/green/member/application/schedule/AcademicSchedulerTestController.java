package com.green.member.application.schedule;

import com.green.common.kafka.member.GraduationCheckResponseEvent;
import com.green.common.model.ResultResponse;
import com.green.member.kafka.GraduationResponseConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * [테스트 전용] 학기 갱신 / 졸업 처리 수동 트리거
 * 테스트 완료 후 삭제할 것
 */
@RestController
@RequestMapping("/test/scheduler")
@RequiredArgsConstructor
public class AcademicSchedulerTestController {

    private final SemesterSchedulerService semesterSchedulerService;
    private final GraduationResponseConsumer graduationResponseConsumer;

    // 학기 갱신 수동 실행
    // POST /test/scheduler/advance-semester
    @PostMapping("/advance-semester")
    public ResultResponse<String> advanceSemester() {
        semesterSchedulerService.advanceSemester();
        return ResultResponse.<String>builder()
                .message("학기 갱신 완료")
                .build();
    }

    // 졸업 처리 수동 실행 (실제 학점 데이터 없이 totalCredits를 직접 지정)
    // POST /test/scheduler/graduate/{studentCode}?credits=120
    @PostMapping("/graduate/{studentCode}")
    public ResultResponse<String> graduate(
            @PathVariable Long studentCode,
            @RequestParam(defaultValue = "120") int credits) {

        GraduationCheckResponseEvent fakeEvent = GraduationCheckResponseEvent.builder()
                .studentCode(studentCode)
                .totalCredits(credits)
                .build();

        boolean graduated = graduationResponseConsumer.consume(fakeEvent);

        String message = graduated
                ? "졸업 처리 완료 (studentCode=" + studentCode + ", credits=" + credits + ")"
                : "졸업 조건 미충족 - 처리 안 됨 (studentCode=" + studentCode + ", credits=" + credits + "/120)";

        return ResultResponse.<String>builder()
                .message(message)
                .build();
    }
}
