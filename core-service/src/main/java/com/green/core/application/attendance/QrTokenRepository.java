package com.green.core.application.attendance;

import com.green.core.entity.attendance.QrToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QrTokenRepository extends JpaRepository<QrToken, Long> {
    // 세션 ID로 가장 최근 토큰 1개 조회 (학생 스캔 시 유효성 검사에 사용)
    // SQL: SELECT * FROM qr_tokens
    //      WHERE attendsession_id = ? ORDER BY expires_at DESC LIMIT 1

    //JPQL로 쓰는법
    @Query("SELECT q FROM QrToken q WHERE q.attendSession.attendsessionId = :sessionId ORDER BY q.expiresAt DESC LIMIT 1")
    Optional<QrToken> findTopBySessionId(@Param("sessionId") Long sessionId);

    Optional<QrToken> findByToken(String token); //학생이 출석하려는 QR의 상태가 유효한지 유효성검사를 위함
}
