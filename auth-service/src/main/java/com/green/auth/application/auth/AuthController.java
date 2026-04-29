package com.green.auth.application.auth;

import com.green.auth.application.auth.model.AuthMemberCreateReq;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtTokenManager jwtTokenManager;
    private final PasswordEncoder passwordEncoder;

    // 로그인
    @PostMapping("/login")
    public ResultResponse<?> login(HttpServletResponse res, @RequestBody LoginReq req,
                                   @RequestHeader(value = "X-Device-Id", defaultValue = "pc") String deviceId) {
        log.info("req: {}", req);

        // DB에 저장된 회원 조회
        AuthMember loginMember = authService.login( req );
        JwtMember jwtMember = new JwtMember( loginMember.getMemberCode(), loginMember.getRole(), deviceId );

//        // DB에 RT 저장 X(redis 사용시)
//        String refreshToken = jwtTokenManager.generateRefreshToken(jwtMember);
//        authService.saveRefreshToken(loginMember, refreshToken);

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
    public ResultResponse<?> logout(HttpServletRequest req, HttpServletResponse res) {
//        // 저장된 유저 정보 가져오기
//        MemberDto memberDto = MemberContext.get();

        // memberDto가 null이 아닐 때만 DB 삭제 로직 수행
//        if (memberDto != null) {
//            authService.deleteRefreshToken(memberDto.memberCode());
//        }

        // 유저 정보 유무와 상관없이 브라우저의 쿠키는 날려줌
        jwtTokenManager.logOut(req, res);

        return ResultResponse.builder()
                .message("로그아웃 되었습니다.")
                .data(1)
                .build();
    }

    // 로그인 유지시 토큰 재발행
    @PostMapping("/reissue")
    public ResultResponse<?> reissue(HttpServletResponse res, HttpServletRequest req) {
        String refreshToken = jwtTokenManager.getRefreshTokenFromCookie(req);
        jwtTokenManager.reissue(req, res);
//        JwtMember jwtMember = authService.reissue(refreshToken);
//        jwtTokenManager.setAccessTokenInCookie(res, jwtMember);

        return ResultResponse.builder()
                .message("Access Token 재발행")
                .data(1)
                .build();
    }


    @PostMapping("/accounts")
    public ResultResponse<?> signup( @RequestBody AuthMemberCreateReq req ) {
        log.info("req: {}", req);
        authService.create( req );
        return ResultResponse.builder()
                .message( "계정 생성 성공" )
                .data( 1 )
                .build();
    }
}