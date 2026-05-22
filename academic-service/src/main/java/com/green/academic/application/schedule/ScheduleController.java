package com.green.academic.application.schedule;

import com.green.academic.application.schedule.model.ScheduleBannerRes;
import com.green.academic.application.schedule.model.ScheduleListReq;
import com.green.academic.application.schedule.model.ScheduleListRes;
import com.green.common.enumcode.EnumScheduleType;
import com.green.common.model.ResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    // CAL-03 학사일정 활성화 상태 조회
    @GetMapping("/active")
    public ResponseEntity<ResultResponse<Map<EnumScheduleType, Boolean>>> getActiveSchedules() {
        Map<EnumScheduleType, Boolean> result = scheduleService.getActiveSchedules();
        return ResponseEntity.ok(new ResultResponse<>("학사일정 활성화 상태 조회 완료", result));
    }

    // 배너용 진행중 학사일정
    @GetMapping("/active/banner")
    public ResponseEntity<ResultResponse<ScheduleBannerRes>> getActiveBannerSchedule() {
        return ResponseEntity.ok(new ResultResponse<>("배너 학사일정 조회 완료",
                scheduleService.getActiveBannerSchedule()));
    }

}