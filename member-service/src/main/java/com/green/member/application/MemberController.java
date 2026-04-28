package com.green.member.application;

import com.green.common.model.ResultResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {
//
//
//    @PostMapping("/test")
//    public ResultResponse<?> signup(@RequestBody MemberCreateReq req ) {
//        log.info("req: {}", req);
//        authService.test( req );
//        return ResultResponse.builder()
//                .message( "회원가입 성공" )
//                .data( 1 )
//                .build();
//    }
}
