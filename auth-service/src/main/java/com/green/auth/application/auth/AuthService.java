package com.green.auth.application.auth;

import com.green.auth.application.auth.model.*;
import com.green.auth.entity.AuthMember;
import com.green.common.exception.AuthErrorCode;
import com.green.common.exception.EmailErrorCode;
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
            throw new BusinessException(AuthErrorCode.INACTIVE_ACCOUNT);
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
                .orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));

        authMember.deactivate(); // isActive -> false
        redisService.deleteAllByMemberCode(memberCode);

        return AuthMemberDeleteRes.builder()
                .memberCode(memberCode)
                .build();
    }

    // 회원 비밀번호 변경
    @Transactional
    public AuthMember updatePassword(long memberCode, PasswordUpdateReq req){
        AuthMember authMember = authMemberRepository.findById(memberCode)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));

        // 비밀번호 검증
        if(!passwordEncoder.matches( req.getOldPassword(), authMember.getPassword() ) ){
            throw new BusinessException(AuthErrorCode.WRONG_PASSWORD);
        }

        // 변경할 비밀번호 암호화
        String hashedPw = passwordEncoder.encode(req.getNewPassword());
        authMember.updatePassword( hashedPw );

        return authMember;
    }

    // 최초 로그인 회원 비밀번호 변경
    @Transactional
    public void updateFirstPassword(long memberCode, PasswordUpdateReq req){
        AuthMember authMember = updatePassword(memberCode, req);
        authMember.updateFirstLogin();
    }

    // 이메일 인증 비밀번호 변경
    @Transactional
    public void resetPassword(PasswordResetReq req){
        if( !redisService.hasKey( "EMAIL-VERIFIED:" + req.getEmail().trim().toLowerCase() ) ){
            throw new BusinessException(EmailErrorCode.NOT_VERIFIED_EMAIL);
        }
        AuthMember authMember = authMemberRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));

        // 기존 비밀번호와 동일 여부 검사
        if(passwordEncoder.matches(req.getNewPassword(), authMember.getPassword()) ){
            throw new BusinessException(AuthErrorCode.SAME_AS_CURRENT_PASSWORD);
        }

        // 변경 비밀번호 암호화
        String hashedPw = passwordEncoder.encode(req.getNewPassword());
        authMember.updatePassword( hashedPw );

        redisService.delete("EMAIL-VERIFIED:" + req.getEmail());

        // 한번도 비밀번호를 변경하지 않았던 경우라면, 최초로그인 false 전환
        if(authMember.getIsFirstLogin() == true){
            authMember.updateFirstLogin();
        }
    }

    // 회원 이메일 변경
    @Transactional
    public void updateEmail(long memberCode, String email){
        AuthMember authMember = authMemberRepository.findById(memberCode)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));
        authMember.updateEmail(email);
    }
    // 회원 로그인 상태 변경
    @Transactional
    public void deactivate(long memberCode){
        AuthMember authMember = authMemberRepository.findById(memberCode)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));
        authMember.deactivate();
    }
}