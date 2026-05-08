package com.green.core.application.attendance;

import com.green.common.model.ResultResponse;
import com.green.core.application.attendance.model.AttendScanReq;
import com.green.core.application.attendance.model.AttendScanRes;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        AttendScanRes res = attendService.scan(req, clientIp);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResultResponse<>("출석 처리되었습니다.", res));
    }

    // X-Forwarded-For 헤더 우선 확인 (프록시/게이트웨이 통과 시 실제 IP 추출)
    private String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            return request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }
}