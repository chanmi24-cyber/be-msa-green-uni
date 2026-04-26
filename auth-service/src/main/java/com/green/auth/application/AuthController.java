package com.green.auth.application;

import com.green.auth.application.model.LoginReq;
import com.green.auth.application.model.LoginRes;
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
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtTokenManager jwtTokenManager;

    // 로그인
    @PostMapping("/login")
    public ResultResponse<?> login(HttpServletResponse res, @RequestBody LoginReq req ) {
        log.info("req: {}", req);

        // DB에 저장된 회원 조회
        AuthMember loginMember = authService.login( req );
        JwtMember jwtMember = new JwtMember( loginMember.getMemberCode(), loginMember.getRole());

        // DB에 RT 저장
        String refreshToken = jwtTokenManager.generateRefreshToken(jwtMember);
        authService.saveRefreshToken(loginMember, refreshToken);

        // 쿠키에 토큰과 유저 정보 저장
        jwtTokenManager.issue(res, jwtMember);

        LoginRes resultData = LoginRes.builder()
                .memberCode(loginMember.getMemberCode())
                .role(loginMember.getRole())
                .isFirstLogin(loginMember.getIsFirstLogin())
                .build();

        return ResultResponse.builder()
                .message("로그인 성공")
                .data(resultData)
                .build();
    }


    // 로그아웃
    @PostMapping("/logout")
    public ResultResponse<?> logout(HttpServletResponse res,
                                    @RequestHeader("X-Member-Id") Integer memberCode) {
//        Integer memberCode = MemberContext.get().getMemberCode();
        authService.logout(memberCode);
        jwtTokenManager.logOut(res);

        return ResultResponse.builder()
                .message("로그아웃")
                .data(1)
                .build();
    }

    // AT 재발행
    @PostMapping("/reissue")
    public ResultResponse<?> reissue(HttpServletResponse res, HttpServletRequest req) {
        jwtTokenManager.reissue(req, res);

        return ResultResponse.builder()
                .message("Access Token 재발행")
                .data(1)
                .build();
    }
}
