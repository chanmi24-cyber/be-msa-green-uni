package com.green.auth.application;

import com.green.auth.application.model.LoginReq;
import com.green.auth.entity.AuthMember;
import com.green.auth.entity.RefreshToken;
import com.green.auth.enumcode.EnumAccountStatus;
import com.green.auth.exception.AuthErrorCode;
import com.green.common.auth.MemberContext;
import com.green.common.constants.ConstJwt;
import com.green.common.exception.BusinessException;
import com.green.common.model.MemberDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    // RT 저장
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


    @Transactional
    public void deleteRefreshToken(Integer memberCode) {
            // RT DB에서 삭제
            refreshTokenRepository.deleteByAuthMember_MemberCode(memberCode);
    }
}