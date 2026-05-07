package com.green.core.application.attendance;

import com.green.common.model.ResultResponse;
import com.green.core.application.attendance.model.AttendSessionEndRes;
import com.green.core.application.attendance.model.AttendSessionStartReq;
import com.green.core.application.attendance.model.AttendSessionStartRes;
import com.green.core.entity.attendance.QrToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/professor/attendances")
public class AttendController {
    private final AttendService attendService;



    // ── ATTD-01 출석 세션 시작 ───────────────────────────────────────────────────
    @PostMapping("{lectureId}/sessions")
    public ResponseEntity<ResultResponse<AttendSessionStartRes>> startSession(
            @PathVariable Long lectureId,
            @RequestBody AttendSessionStartReq req) {
        AttendSessionStartRes res = attendService.startSession(lectureId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResultResponse<>("출석 세션이 시작되었습니다.", res));
    }

    // ── ATTD-07 QR 토큰 SSE ─────────────────────────────────────────────────────
    @GetMapping(value = "sessions/{sessionId}/token", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getQrTokenStream(@PathVariable Long sessionId) {

        // SseEmitter: 클라이언트와 SSE 연결을 유지하는 객체
        // 900_000L = 15분(900초 * 1000밀리초) 동안 연결 유지
        SseEmitter emitter = new SseEmitter(900_000L); //15분 타임아웃

        // ScheduledExecutorService: 일정 시간마다 코드를 반복 실행하는 스케줄러
        // Executors.newSingleThreadScheduledExecutor() = 스레드 1개짜리 스케줄러 생성
        // (스레드: 코드를 실행하는 일꾼. 별도 스레드에서 5초마다 토큰 전송)
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

      ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(() -> {
        try {
          // UUID 랜덤 생성 → DB INSERT → 저장된 토큰 반환
          QrToken qrToken = attendService.createAndSaveToken(sessionId);

          emitter.send(
              SseEmitter.event()
                  .data("{\"token\":\"" + qrToken.getToken() + "\","
                      + "\"expiresAt\":\"" + qrToken.getExpiresAt() + "\"}")
          );

          log.info("QR 토큰 DB 저장 및 전송 완료 - sessionId: {}", sessionId);

          //catch를 IOException만 하게되면 IO오류만 잡게되어서 아래에 나머지 오류를 잡도록 Exception 추가
        } catch (IOException e) {
          log.info("SSE 클라이언트 연결 끊김 - sessionId: {}", sessionId);
          emitter.completeWithError(e);
          scheduler.shutdown();

        } catch (Exception e) {
          // JPA 오류, RuntimeException 등 나머지 모든 예외
          // 예: 세션이 DB에 없는 경우, DB 연결 오류 등
          log.error("QR 토큰 생성 중 오류 발생 - sessionId: {}, 오류: {}", sessionId, e.getMessage());
          emitter.completeWithError(e);
          scheduler.shutdown();
        }
      }, 0, 5, TimeUnit.SECONDS);

      emitter.onCompletion(() -> {
        task.cancel(true);
        scheduler.shutdown();
      });

      emitter.onTimeout(() -> {
        task.cancel(true);
        scheduler.shutdown();
      });

        // 5초마다 새 토큰 생성해서 emitter.send()로 push
        return emitter;
    }

    // ── ATTD-02 출석 세션 종료 ───────────────────────────────────────────────────
    @PatchMapping("{lectureId}/sessions/{sessionId}")
    public ResponseEntity<ResultResponse<AttendSessionEndRes>> endSession(
            @PathVariable Long lectureId,
            @PathVariable Long sessionId) {
        AttendSessionEndRes res = attendService.endSession(lectureId, sessionId);
        return ResponseEntity.ok(new ResultResponse<>("출석이 종료되었습니다.", res));
    }
}
