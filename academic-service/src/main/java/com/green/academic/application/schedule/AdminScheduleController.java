package com.green.academic.application.schedule;

import com.green.academic.application.schedule.model.ScheduleCreateReq;
import com.green.academic.application.schedule.model.ScheduleUpdateReq;
import com.green.academic.application.schedule.model.ScheduleUpdateRes;
import com.green.common.model.ResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/schedules")
public class AdminScheduleController {
    private final ScheduleService scheduleService;

    // CAL-01 학사일정 등록
    @PostMapping
    public ResponseEntity<ResultResponse<Void>> createSchedule(
            @RequestBody ScheduleCreateReq req) {
        scheduleService.createSchedule(req);
        return ResponseEntity.ok(new ResultResponse<>("학사일정 등록 완료", null));
    }

    @PatchMapping("/{scheduleId}")
    public ResponseEntity<ResultResponse<ScheduleUpdateRes>> updateSchedule(
            @PathVariable Long scheduleId,
            @RequestBody ScheduleUpdateReq req) {
        return ResponseEntity.ok(new ResultResponse<>("학사일정 변경 완료", scheduleService.updateSchedule(scheduleId, req)));
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<ResultResponse<Void>> deleteSchedule(
            @PathVariable Long scheduleId) {
        scheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.ok(new ResultResponse<>("학사일정 삭제 완료", null));
    }

}