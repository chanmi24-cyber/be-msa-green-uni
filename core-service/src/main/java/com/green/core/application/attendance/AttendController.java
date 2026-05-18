package com.green.core.application.attendance;

import com.green.common.model.ResultResponse;
import com.green.core.application.attendance.model.AttendActiveSessionRes;
import com.green.core.application.attendance.model.AttendCancelHistoryRes;
import com.green.core.application.attendance.model.AttendMakeupReq;
import com.green.core.application.attendance.model.AttendTodaySessionRes;
import com.green.core.application.attendance.model.AttendLectureRes;
import com.green.core.application.attendance.model.AttendProListRes;
import com.green.core.application.attendance.model.AttendSessionEndRes;
import com.green.core.application.attendance.model.AttendSessionListRes;
import com.green.core.application.attendance.model.AttendSessionReq;
import com.green.core.application.attendance.model.AttendSessionRes;
import com.green.core.application.attendance.model.AttendStatusUpdateReq;
import com.green.core.entity.attendance.QrToken;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import org.springframework.format.annotation.DateTimeFormat;

import java.io.IOException;
import java.time.LocalDate;
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



    // ── 교수 강의 목록 조회 (출석 QR 선택 화면용) ────────────────────────────────
    @GetMapping("my-lectures")
    public ResponseEntity<ResultResponse<List<AttendLectureRes>>> getProfessorLectures() {
        return ResponseEntity.ok(new ResultResponse<>("교수 강의 목록 조회 성공", attendService.getProfessorLectures()));
    }

    // ── 특정 강의 정보 조회 (QR 출석 페이지 헤더용) ──────────────────────────────
    @GetMapping("my-lectures/{lectureId}")
    public ResponseEntity<ResultResponse<AttendLectureRes>> getProfessorLecture(@PathVariable Long lectureId) {
        return ResponseEntity.ok(new ResultResponse<>("강의 정보 조회 성공", attendService.getProfessorLecture(lectureId)));
    }

    // ── 오늘 세션 조회 (활성·종료 무관 — QR 페이지 진입 시 세션 상태 복구) ──────
    @GetMapping("{lectureId}/sessions/today")
    public ResponseEntity<ResultResponse<AttendTodaySessionRes>> getTodaySession(@PathVariable Long lectureId) {
        return ResponseEntity.ok(new ResultResponse<>("오늘 세션 조회", attendService.getTodaySession(lectureId).orElse(null)));
    }

    // ── 활성 세션 조회 (페이지 재진입 시 기존 세션 복구) ────────────────────────
    @GetMapping("{lectureId}/sessions/active")
    public ResponseEntity<ResultResponse<AttendActiveSessionRes>> getActiveSession(@PathVariable Long lectureId) {
        return ResponseEntity.ok(new ResultResponse<>("활성 세션 조회", attendService.getActiveSession(lectureId).orElse(null)));
    }

    // ── ATTD-01 출석 세션 시작 ───────────────────────────────────────────────────
    // [수정] AttendSessionStartReq → AttendSessionReq, AttendSessionStartRes → AttendSessionRes
    @PostMapping("{lectureId}/sessions")
    public ResponseEntity<ResultResponse<AttendSessionRes>> startSession(
            @PathVariable Long lectureId,
            @RequestBody AttendSessionReq req) {
        AttendSessionRes res = attendService.startSession(lectureId, req);
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

          // QR 코드에 인코딩할 토큰 UUID만 전송 (JSON 전체를 보내면 학생 스캔 시 UUID 파싱 불가)
          emitter.send(SseEmitter.event().data(qrToken.getToken()));

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

    // ── 출석부 세션 목록 조회 ────────────────────────────────────────────────────
    @GetMapping("{lectureId}/sessions")
    public ResponseEntity<ResultResponse<List<AttendSessionListRes>>> getSessionList(
            @PathVariable Long lectureId) {
        return ResponseEntity.ok(new ResultResponse<>("세션 목록 조회 성공", attendService.getSessionList(lectureId)));
    }

    // ── 세션별 출석부 조회 ───────────────────────────────────────────────────────
    @GetMapping("{lectureId}/sessions/{sessionId}/roster")
    public ResponseEntity<ResultResponse<List<AttendProListRes>>> getRoster(
            @PathVariable Long lectureId,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(new ResultResponse<>("출석부 조회 성공", attendService.getRoster(lectureId, sessionId)));
    }

    // ── 출석 기록 날짜 목록 조회 (달력 연두색 하이라이트용) ────────────────────────
    @GetMapping("{lectureId}/dates")
    public ResponseEntity<ResultResponse<List<String>>> getRecordedDates(@PathVariable Long lectureId) {
        return ResponseEntity.ok(new ResultResponse<>("출석 기록 날짜 조회 성공", attendService.getRecordedDates(lectureId)));
    }

    // ── ATTD-05 날짜별 출석부 조회 (GET /{lectureId}?attendDate=YYYY-MM-DD) ──────────
    // attendDate 생략 시 오늘 날짜 기본값 적용
    @GetMapping("{lectureId}")
    public ResponseEntity<ResultResponse<List<AttendProListRes>>> getRosterByDate(
            @PathVariable Long lectureId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate attendDate) {
        if (attendDate == null) attendDate = LocalDate.now();
        return ResponseEntity.ok(new ResultResponse<>("날짜별 출석부 조회 성공", attendService.getRosterByDate(lectureId, attendDate)));
    }

    // ── ATTD-08 휴강 처리 (POST /{lectureId}/cancels) ────────────────────────────
    // [추가]
    @PostMapping("{lectureId}/cancels")
    public ResponseEntity<ResultResponse<Void>> cancelClass(
            @PathVariable Long lectureId,
            @RequestBody AttendSessionReq req) {
        attendService.cancelClass(lectureId, req.getClassDate());
        return ResponseEntity.ok(new ResultResponse<>("휴강 처리되었습니다.", null));
    }

    // ── ATTD-10 휴강 내역 조회 (GET /{lectureId}/cancels) ─────────────────────────
    // [추가]
    @GetMapping("{lectureId}/cancels")
    public ResponseEntity<ResultResponse<List<AttendCancelHistoryRes>>> getCancelHistory(
            @PathVariable Long lectureId) {
        return ResponseEntity.ok(new ResultResponse<>("휴강 내역 조회 성공", attendService.getCancelHistory(lectureId)));
    }

    // ── ATTD-09 보강 세션 시작 (POST /{lectureId}/makeups) ────────────────────────
    // [추가]
    @PostMapping("{lectureId}/makeups")
    public ResponseEntity<ResultResponse<AttendSessionRes>> startMakeupSession(
            @PathVariable Long lectureId,
            @RequestBody AttendMakeupReq req) {
        AttendSessionRes res = attendService.startMakeupSession(lectureId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResultResponse<>("보강 세션이 시작되었습니다.", res));
    }

    // ── ATTD-06 출석 상태 일괄 수정 (PATCH /{lectureId}) ─────────────────────────────
    @PatchMapping("{lectureId}")
    public ResponseEntity<ResultResponse<Void>> updateAttendStatuses(
            @PathVariable Long lectureId,
            @RequestBody List<AttendStatusUpdateReq> reqs) {
        attendService.updateAttendStatuses(lectureId, reqs);
        return ResponseEntity.ok(new ResultResponse<>("출석 상태가 수정되었습니다.", null));
    }

}
