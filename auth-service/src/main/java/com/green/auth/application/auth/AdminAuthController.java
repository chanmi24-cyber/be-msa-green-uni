package com.green.auth.application.auth;

import com.green.auth.application.auth.enumcode.DeviceType;
import com.green.auth.application.auth.model.LoginReq;
import com.green.auth.application.auth.model.LoginRes;
import com.green.auth.entity.AuthMember;
import com.green.common.enumcode.EnumMemberRole;
import com.green.common.exception.AuthErrorCode;
import com.green.common.exception.BusinessException;
import com.green.common.model.JwtMember;
import com.green.common.model.ResultResponse;
import com.green.common.security.JwtTokenManager;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminAuthController {
    private final AuthService authService;
    private final JwtTokenManager jwtTokenManager;

    // 로그인
    @PostMapping("/login")
    public ResultResponse<?> login(HttpServletResponse res, @RequestBody @Valid LoginReq req,
                                   @RequestHeader("X-Device-Id") DeviceType deviceType) {
        // DB에 저장된 회원 조회
        AuthMember loginMember = authService.login( req );
        // 학생/교수 로그인 불가
        if (loginMember.getRole() != EnumMemberRole.ADMIN) {
            throw new BusinessException(AuthErrorCode.LOGIN_UNAUTHORIZED_ROLE);
        }
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
}
