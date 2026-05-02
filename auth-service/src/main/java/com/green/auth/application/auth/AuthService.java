package com.green.auth.application.auth;

import com.green.auth.application.auth.model.AuthMemberCreateReq;
import com.green.auth.application.auth.model.LoginReq;
import com.green.auth.entity.AuthMember;
import com.green.auth.enumcode.EnumAccountStatus;
import com.green.common.auth.AuthErrorCode;
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
    private final PasswordEncoder passwordEncoder;

    // 로그인
    @Transactional
    public AuthMember login(LoginReq req) {
        // 회원 조회
        AuthMember loginMember = authMemberRepository.findById(req.getMemberCode())
                .orElseThrow(() -> new BusinessException(AuthErrorCode.LOGIN_FAIL));

        // 비밀번호 검증 (존재 여부 및 비밀번호 일치 확인)
        if (!passwordEncoder.matches(req.getPassword(), loginMember.getPassword())) {
            throw new BusinessException(AuthErrorCode.LOGIN_FAIL);
        }

        // 계정 상태 검증
        if (loginMember.getAccountStatus() == EnumAccountStatus.TERMINATED) {
            throw new BusinessException(AuthErrorCode.ACCOUNT_TERMINATED);
        }
        return loginMember;
    }

    public void createAuthMember(AuthMemberCreateReq req) {
        String hashedPassword = passwordEncoder.encode(req.getPassword());

        AuthMember newMember = AuthMember.builder()
                .memberCode( req.getMemberCode() )
                .role( req.getRole() )
                .email( req.getEmail() )
                .password( hashedPassword )
                .build();

        authMemberRepository.save(newMember);

    }
}