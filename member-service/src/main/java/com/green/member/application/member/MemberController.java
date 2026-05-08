package com.green.member.application.member;

import com.green.common.auth.MemberContext;
import com.green.common.model.MemberDto;
import com.green.common.model.ResultResponse;
import com.green.member.application.member.model.MemberProfileRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    // 로그인 멤버 프로파일 조회
    @GetMapping("/my")
    public ResultResponse<?> findLoginUserProfile(){
        MemberDto loginMember = MemberContext.get();
        log.info("loginMember: {}", loginMember);
        MemberProfileRes res = memberService.getMyProfile(loginMember.memberCode(), loginMember.role());
        return ResultResponse.builder()
                .message("프로파일 조회 ")
                .data(res)
                .build();
    }
}
