package com.green.auth.application.auth;

import com.green.auth.application.auth.model.*;
import com.green.auth.entity.AuthMember;
import com.green.common.auth.AuthErrorCode;
import com.green.common.exception.BusinessException;
import com.green.common.redis.RedisService;
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
    private final RedisService redisService;

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
        if (!loginMember.getIsActive()) {
            throw new BusinessException(AuthErrorCode.ACCOUNT_TERMINATED);
        }
        return loginMember;
    }

    // 계정 생성
    public AuthMemberCreateRes createAuthMember(AuthMemberCreateReq req) {
        String hashedPassword = passwordEncoder.encode(req.getPassword());

        AuthMember newMember = AuthMember.builder()
                .memberCode( req.getMemberCode() )
                .role( req.getRole() )
                .email( req.getEmail() )
                .password( hashedPassword )
                .build();

        AuthMember saved = authMemberRepository.save(newMember);

        return AuthMemberCreateRes.builder()
                .memberCode( saved.getMemberCode( ))
                .build();
    }

    // 계정 비활성화
    @Transactional
    public AuthMemberDeleteRes deleteAuthMember(Long memberCode) {
        AuthMember authMember = authMemberRepository.findById(memberCode)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.ACCOUNT_TERMINATED));

        authMember.deactivate(); // isActive -> false
        redisService.deleteAllByMemberCode(memberCode);

        return AuthMemberDeleteRes.builder()
                .memberCode(memberCode)
                .build();
    }
}