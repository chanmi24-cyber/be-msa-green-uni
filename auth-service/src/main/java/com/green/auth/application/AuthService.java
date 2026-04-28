package com.green.auth.application;

import com.green.auth.application.model.LoginReq;
import com.green.auth.application.model.MemberCreateReq;
import com.green.auth.entity.AuthMember;
import com.green.auth.entity.RefreshToken;
import com.green.auth.enumcode.EnumAccountStatus;
import com.green.auth.exception.AuthErrorCode;
import com.green.common.constants.ConstJwt;
import com.green.common.constants.EventType;
import com.green.common.exception.BusinessException;
import com.green.common.model.EnumMemberRole;
import com.green.common.model.JwtMember;
import com.green.common.model.MemberEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthMemberRepository authMemberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConstJwt constJwt;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // 로그인
    @Transactional
    public AuthMember login(LoginReq req) {
        // 1. 회원 조회
        AuthMember loginMember = authMemberRepository.findById(req.getMemberCode())
                .orElseThrow(() -> new BusinessException(AuthErrorCode.LOGIN_FAIL));

        // 2. 검증 (존재 여부 및 비밀번호 일치 확인)
        if (loginMember == null || !passwordEncoder.matches(req.getPassword(), loginMember.getPassword())) {
            throw new BusinessException(AuthErrorCode.LOGIN_FAIL);
        }

        // 3. 계정 상태 검증
        if (loginMember.getAccountStatus() == EnumAccountStatus.TERMINATED) {
            throw new BusinessException(AuthErrorCode.ACCOUNT_TERMINATED);
        }

        return loginMember;
    }

    // 로그인 시 RT DB에 저장
    @Transactional
    public void saveRefreshToken(AuthMember authMember, String refreshToken) {
        // 기존 RT 삭제 (재로그인 시 이전 RT 제거)
        refreshTokenRepository.deleteByAuthMember_MemberCode(authMember.getMemberCode());

        RefreshToken rt = RefreshToken.builder()
                .authMember(authMember)
                .tokenValue(refreshToken)
                .expiresAt(LocalDateTime.now().plusSeconds(constJwt.refreshTokenCookieValiditySeconds()))
                .build();

        refreshTokenRepository.save(rt);
    }

    // 로그아웃 시 RT DB에서 삭제
    @Transactional
    public void deleteRefreshToken(Integer memberCode) {
        refreshTokenRepository.deleteByAuthMember_MemberCode(memberCode);
    }

    @Transactional
    public JwtMember reissue(String refreshToken) {
        // DB에서 RT 존재 여부 확인
        RefreshToken savedRt = refreshTokenRepository.findByTokenValue(refreshToken);
        if (savedRt == null) {
            throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
        // RT 파싱해서 JwtMember 반환 (JwtTokenManager에서 처리)
        return new JwtMember(savedRt.getAuthMember().getMemberCode(),
                savedRt.getAuthMember().getRole());
    }

    public void test(MemberCreateReq req) {
        String hashedPassword = passwordEncoder.encode(req.getPassword());

        AuthMember newMember = new AuthMember();
        newMember.setMemberCode(req.getMemberCode());
        newMember.setEmail(req.getEmail());
        newMember.setPassword(hashedPassword);

        authMemberRepository.save(newMember);

        MemberEvent userEvent = MemberEvent.builder()
                .memberCode(newMember.getMemberCode())
                .role(newMember.getRole())
                .eventType( EventType.E_CREATED )
                .build();

        kafkaSend(userEvent);
    }

    private void kafkaSend(MemberEvent memberEvent) {
        kafkaTemplate.send("kafka-test", String.valueOf(memberEvent.getMemberCode()), memberEvent)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        // 성공 시 로그
                        log.info("✅ [Kafka Success] Topic: {}, Offset: {}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().offset());
                    } else {
                        // 실패 시 로그
                        log.error("❌ [Kafka Failure] 원인: {}", ex.getMessage());
                    }
                });
    }
}