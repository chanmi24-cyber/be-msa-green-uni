package com.green.core.application.attendance;

import com.green.core.entity.attendance.AttendanceSession;
import com.green.core.entity.attendance.QrToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendService {
    private final QrTokenRepository qrTokenRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;

    /**
     * QR 토큰 생성 + DB 저장
     *
     * 흐름:
     * 1) UUID로 랜덤 토큰 문자열 생성
     * 2) 만료 시각 = 지금 + 5초 로 설정
     * 3) qr_tokens 테이블에 INSERT
     * 4) 저장된 토큰 반환 → SSE로 프론트에 전송
     *
     * @param sessionId - 어느 세션의 토큰인지
     * @return QrToken  - 저장 완료된 토큰 엔티티
     */


    @Transactional  // DB 작업 중 오류 시 자동 롤백
    public QrToken createAndSaveToken(Long sessionId) {

      // sessionId로 AttendanceSession 객체를 먼저 조회
      AttendanceSession session = attendanceSessionRepository.findById(sessionId)
          .orElseThrow(() -> new RuntimeException("존재하지 않는 세션입니다. sessionId: " + sessionId));

      String tokenValue = UUID.randomUUID().toString();
      LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(5);

      QrToken qrToken = QrToken.builder()
          .attendSession(session)  //()에 Long이 아니라 AttendanceSession 객체를 넣음
          .token(tokenValue)
          .expiresAt(expiresAt)
          .build();

      return qrTokenRepository.save(qrToken);
    }

    /**
     * 학생 스캔 시 토큰 유효성 검사
     *
     * 검사 항목:
     * 1) DB에 해당 토큰이 존재하는가?
     * 2) 만료 시각이 현재 시각보다 이후인가? (아직 살아있는 토큰인가?)
     *
     * @param token - 학생이 스캔한 QR에서 읽어온 토큰 문자열
     * @return true: 유효한 토큰 / false: 만료되었거나 존재하지 않는 토큰
     */
    public boolean isTokenValid(String token) {
      return qrTokenRepository.findByToken(token)
          .map(qrToken -> qrToken.getExpiresAt().isAfter(LocalDateTime.now()))
          .orElse(false); // 토큰이 DB에 없으면 false
    }
}
