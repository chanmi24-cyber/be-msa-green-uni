package com.green.core.application.attendance;

import com.green.common.model.ResultResponse;
import com.green.core.application.attendance.model.AttendScanReq;
import com.green.core.application.attendance.model.AttendScanRes;
import com.green.core.application.attendance.model.AttendStuListRes;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/student/attendances")
public class AttendStudentController {

    private final AttendService attendService;

    // ── ATTD-03 학생 QR 출석 스캔 ───────────────────────────────────────────────
    @PostMapping("/scan")
    public ResponseEntity<ResultResponse<AttendScanRes>> scan(
            @RequestBody AttendScanReq req,
            HttpServletRequest request) {
        String clientIp = extractClientIp(request);
        log.warn("[SCAN-IP] X-Real-IP={} | X-Forwarded-For={} | remoteAddr={} | extracted={}",
                request.getHeader("X-Real-IP"), request.getHeader("X-Forwarded-For"),
                request.getRemoteAddr(), clientIp);
        AttendScanRes res = attendService.scan(req, clientIp);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResultResponse<>("출석 처리되었습니다.", res));
    }

    // ── ATTD-04 학생 본인 출석 조회 ─────────────────────────────────────────────
    // lectureId 없으면 수강 중인 전체 강의, 있으면 해당 강의만 반환
    @GetMapping("/my")
    public ResponseEntity<ResultResponse<List<AttendStuListRes>>> getMyAttendance(
            @RequestParam(required = false) Long lectureId) {
        return ResponseEntity.ok(new ResultResponse<>("내 출석 조회 성공", attendService.getMyAttendance(lectureId)));
    }

    // X-Real-IP(gateway 직접 주입) → X-Forwarded-For → remoteAddr 순으로 실제 IP 추출
    private String extractClientIp(HttpServletRequest request) {
        // gateway의 TokenAuthenticationFilter가 request.getRemoteAddr()로 직접 주입
        String ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isBlank()) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        // IPv6 매핑된 IPv4 처리 (예: ::ffff:192.168.0.1 → 192.168.0.1)
        if (ip.startsWith("::ffff:")) {
            ip = ip.substring(7);
        }
        return ip;
    }
}