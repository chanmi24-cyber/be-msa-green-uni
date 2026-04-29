package com.green.member.application.controller;

import com.green.common.model.ResultResponse;
import com.green.member.application.model.StudentCreateReq;
import com.green.member.application.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/members")
public class AdminMemberController {
    private final MemberService memberService;

    @PostMapping
    public ResultResponse<?> test(@RequestBody StudentCreateReq req ) {
        log.info("req: {}", req);
        memberService.test( req );
        return ResultResponse.builder()
                .message( "학생 생성 테스트 성공" )
                .data( 1 )
                .build();
    }
}
