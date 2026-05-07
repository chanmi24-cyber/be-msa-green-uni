package com.green.academic.application.schedule;

import com.green.academic.application.schedule.model.ScheduleCreateReq;
import com.green.common.model.ResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}