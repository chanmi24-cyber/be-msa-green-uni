package com.green.auth.application.auth;

import com.green.auth.application.auth.model.AuthMemberCreateReq;
import com.green.auth.application.auth.model.AuthMemberCreateRes;
import com.green.auth.application.auth.model.LoginReq;
import com.green.auth.application.auth.model.LoginRes;
import com.green.common.security.JwtTokenManager;
import com.green.auth.entity.AuthMember;
import com.green.common.model.JwtMember;
import com.green.common.model.ResultResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    public ResultResponse<?> login(HttpServletResponse res, @RequestBody LoginReq req,
                                   @RequestHeader(value = "X-Device-Id", defaultValue = "pc") String deviceId) {
        log.info("req: {}", req);

        // DB에 저장된 회원 조회
        AuthMember loginMember = authService.login( req );
        // 토큰에 담을 유저 정보 세팅
        JwtMember jwtMember = new JwtMember( loginMember.getMemberCode(), loginMember.getRole().getCode(), deviceId );

        // AT/RT 생성 후 쿠키와 Redis에 저장
        jwtTokenManager.issue(res, jwtMember);

        LoginRes resultData = LoginRes.builder()
                .memberCode( loginMember.getMemberCode( ))
                .role(loginMember.getRole())
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
                .data(1)
                .build();
    }

    // 로그인 유지 AT 만료 시 RT로 재발급
    @PostMapping("/reissue")
    public ResultResponse<?> reissue(HttpServletResponse res, HttpServletRequest req) {
        // 쿠키의 RT를 Redis에 저장된 RT와 비교 검증 후 새 AT 발급
        jwtTokenManager.reissue(req, res);
        return ResultResponse.builder()
                .message("Access Token 재발행")
                .data(1)
                .build();
    }
}