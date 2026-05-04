package com.green.member.application.admin;

import com.green.member.application.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final MemberService memberService;

//    @PostMapping
//    public ResultResponse<?> test(@RequestBody StudentCreateReq req ) {
//        log.info("req: {}", req);
//        memberService.test( req );
//        return ResultResponse.builder()
//                .message( "학생 생성 테스트 성공" )
//                .data( 1 )
//                .build();
//    }
}
