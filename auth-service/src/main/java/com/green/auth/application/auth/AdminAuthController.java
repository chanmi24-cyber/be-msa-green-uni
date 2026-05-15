package com.green.auth.application.auth;

import com.green.auth.application.auth.model.AuthMemberCreateReq;
import com.green.auth.application.auth.model.AuthMemberCreateRes;
import com.green.auth.application.auth.model.AuthMemberDeleteRes;
import com.green.common.model.ResultResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminAuthController {
    private final AuthService authService;

//    @PostMapping("/accounts")
//    public ResultResponse<?> createAccount(@RequestBody AuthMemberCreateReq req ) {
//        log.info("req: {}", req);
//        AuthMemberCreateRes res = authService.createAuthMember( req );
//        return ResultResponse.builder()
//                .message( "계정 생성 성공" )
//                .data( res )
//                .build();
//    }
//
//    @DeleteMapping("/accounts/{memberCode}")
//    public ResultResponse<?> deleteAccount(@PathVariable Long memberCode){
//        AuthMemberDeleteRes res = authService.deleteAuthMember(memberCode);
//        return ResultResponse.builder()
//                .message( "계정 비활성화" )
//                .data( res )
//                .build();
//    }
}
