package com.green.core.application.attendance;

import com.green.core.entity.attendance.QrToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
