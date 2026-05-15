package com.green.auth.application.auth;

import com.green.auth.application.auth.enumcode.DeviceType;
import com.green.auth.application.auth.model.*;
import com.green.common.auth.MemberContext;
import com.green.common.model.MemberDto;
import com.green.common.security.JwtTokenManager;
import com.green.auth.entity.AuthMember;
import com.green.common.model.JwtMember;
import com.green.common.model.ResultResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtTokenManager jwtTokenManager;

    // 로그인
    @PostMapping("/login")
    public ResultResponse<?> login(HttpServletResponse res, @RequestBody @Valid LoginReq req,
                                   @RequestHeader("X-Device-Id") DeviceType deviceType) {
        // DB에 저장된 회원 조회
        AuthMember loginMember = authService.login( req );
        // 토큰에 담을 유저 정보 세팅
        JwtMember jwtMember = new JwtMember( loginMember.getMemberCode(), loginMember.getRole().getCode(), deviceType.name() );
        // AT/RT 생성 후 쿠키와 Redis에 저장
        jwtTokenManager.issue(res, jwtMember);
        LoginRes resultData = LoginRes.builder()
                .memberCode( loginMember.getMemberCode( ))
                .deviceId( jwtMember.getDeviceId() )
                .role(loginMember.getRole().getCode())
                .isFirstLogin(loginMember.getIsFirstLogin())
                .build();
        log.info("loginRes: {}", resultData);
        return ResultResponse.builder()
                .message("로그인 성공")
                .data(resultData)
                .build();
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResultResponse<?> logout(HttpServletRequest req, HttpServletResponse res) {
        // 쿠키의 RT에서 memberCode/deviceId 꺼내 Redis 삭제 후 AT/RT 쿠키도 삭제
        jwtTokenManager.logOut(req, res);
        return ResultResponse.builder()
                .message("로그아웃 되었습니다.")
                .build();
    }

    // 로그인 유지 AT 만료 시 RT로 재발급
    @PostMapping("/reissue")
    public ResultResponse<?> reissue(HttpServletResponse res, HttpServletRequest req) {
        // 쿠키의 RT를 Redis에 저장된 RT와 비교 검증 후 새 AT 발급
        jwtTokenManager.reissue(req, res);
        return ResultResponse.builder()
                .message("Access Token 재발행")
                .build();
    }

    // 회원 비밀번호 변경
    @PatchMapping("/passwords")
    public ResultResponse<?> updatePassword(@RequestBody @Valid PasswordUpdateReq req){
        MemberDto loginMember = MemberContext.get();
        authService.updatePassword( loginMember.memberCode(), req );
        authService.deleteOtherSessions( loginMember.memberCode(), loginMember.deviceId() ); // 현재 기기 제외 세션 무효화
        return ResultResponse.builder()
                .message("비밀번호 변경이 완료되었습니다")
                .build();
    }

    // 최초 회원 비밀번호 변경
    @PatchMapping("/passwords/first")
    public ResultResponse<?> updateFirstPassword(@RequestBody @Valid PasswordUpdateReq req){
        MemberDto loginMember = MemberContext.get();
        authService.updateFirstPassword( loginMember.memberCode(), req );
        // 세션 삭제 없음
        return ResultResponse.builder()
                .message("최초 로그인 회원 비밀번호 변경")
                .build();
    }

    // 이메일 인증 비밀번호 변경
    @PatchMapping("/passwords/reset")
    public ResultResponse<?> resetPassword(@RequestBody @Valid PasswordResetReq req){
        authService.resetPassword( req );
        return ResultResponse.builder()
                .message("비밀번호 변경이 완료되었습니다")
                .build();
    }
}