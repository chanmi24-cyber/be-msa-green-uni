package com.green.member.application.member;

import com.green.common.auth.MemberContext;
import com.green.common.model.MemberDto;
import com.green.common.model.ResultResponse;
import com.green.member.application.member.model.MemberProfileRes;
import com.green.member.application.member.model.MemberUpdateReq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
                .message("프로파일 조회")
                .data(res)
                .build();
    }

    // 로그인 멤버 내 정보 수정
    @PatchMapping("/my")
    public ResultResponse<?> updateMyProfile(
            @RequestPart @Valid MemberUpdateReq req,
            @RequestPart(required = false) MultipartFile pic) {
        MemberDto loginMember = MemberContext.get();
        memberService.updateMyProfile(loginMember.memberCode(), loginMember.role(), req, pic);
        return ResultResponse.builder()
                .message("내 정보 수정을 완료했습니다")
                .build();
    }
}
