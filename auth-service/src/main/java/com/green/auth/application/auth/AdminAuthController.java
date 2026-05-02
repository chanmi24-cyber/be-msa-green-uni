package com.green.auth.application.auth;

import com.green.auth.application.auth.model.AuthMemberCreateReq;
import com.green.common.model.ResultResponse;
import com.green.common.security.JwtTokenManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminAuthController {
    private final AuthService authService;
    private final JwtTokenManager jwtTokenManager;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/accounts")
    public ResultResponse<?> createAccount(@RequestBody AuthMemberCreateReq req ) {
        log.info("req: {}", req);
        authService.createAuthMember( req );
        return ResultResponse.builder()
                .message( "계정 생성 성공" )
                .data( 1 )
                .build();
    }
}
