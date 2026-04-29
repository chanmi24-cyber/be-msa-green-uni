package com.green.auth.application.auth;

import com.green.auth.application.auth.model.AuthMemberCreateReq;
import com.green.auth.application.auth.model.LoginReq;
import com.green.auth.entity.AuthMember;
import com.green.auth.enumcode.EnumAccountStatus;
import com.green.common.auth.AuthErrorCode;
import com.green.auth.repository.AuthMemberRepository;
import com.green.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthMemberRepository authMemberRepository;
//    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
//    private final ConstJwt constJwt;

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
//    @Transactional
//    public void saveRefreshToken(AuthMember authMember, String refreshToken) {
//        // 기존 RT 삭제 (재로그인 시 이전 RT 제거)
//        refreshTokenRepository.deleteByAuthMember_MemberCode(authMember.getMemberCode());
//
//        RefreshToken rt = RefreshToken.builder()
//                .authMember(authMember)
//                .tokenValue(refreshToken)
//                .expiresAt(LocalDateTime.now().plusSeconds(constJwt.refreshTokenCookieValiditySeconds()))
//                .build();
//
//        refreshTokenRepository.save(rt);
//    }

//    // 로그아웃 시 RT DB에서 삭제
//    @Transactional
//    public void deleteRefreshToken(Integer memberCode) {
//            refreshTokenRepository.deleteByAuthMember_MemberCode(memberCode);
//    }

//    @Transactional
//    public JwtMember reissue(String refreshToken) {
//        // DB에서 RT 존재 여부 확인
//        RefreshToken savedRt = refreshTokenRepository.findByTokenValue(refreshToken);
//        if (savedRt == null) {
//            throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
//        }
//        // RT 파싱해서 JwtMember 반환 (JwtTokenManager에서 처리)
//        return new JwtMember(savedRt.getAuthMember().getMemberCode(),
//                savedRt.getAuthMember().getRole());
//    }



    public void create(AuthMemberCreateReq req) {
        String hashedPassword = passwordEncoder.encode(req.getPassword());

        AuthMember newMember = new AuthMember();
        newMember.setMemberCode(req.getMemberCode());
        newMember.setRole( req.getRole() );
        newMember.setEmail(req.getEmail());
        newMember.setPassword(hashedPassword);

        authMemberRepository.save(newMember);

    }
}