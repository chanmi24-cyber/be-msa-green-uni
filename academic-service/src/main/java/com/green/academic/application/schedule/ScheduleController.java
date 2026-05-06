package com.green.academic.application.schedule;

import com.green.academic.application.schedule.model.ScheduleListReq;
import com.green.academic.application.schedule.model.ScheduleListRes;
import com.green.common.model.ResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;

    // CAL-02 학사일정 조회
    @GetMapping
    public ResponseEntity<ResultResponse<List<ScheduleListRes>>> getSchedules(
            @ModelAttribute ScheduleListReq req) {
        List<ScheduleListRes> result = scheduleService.getSchedules(req);
        return ResponseEntity.ok(new ResultResponse<>("학사일정 조회 완료", result));
    }
}