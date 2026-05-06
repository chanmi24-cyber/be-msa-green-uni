package com.green.member.application.admin;

import com.green.common.model.ResultResponse;
import com.green.member.application.member.MemberService;
import com.green.member.application.member.model.MemberCreateReq;
import com.green.member.application.member.model.MemberCreateRes;
import com.green.member.application.student.model.StudentCreateReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final MemberService memberService;

    @PostMapping("/students")
    public ResultResponse<?> createStudent(@RequestPart StudentCreateReq req,
                                           @RequestPart(required = false) MultipartFile pic) {
        MemberCreateRes res = memberService.createStudent(req, pic);
        return ResultResponse.builder()
                .message("학생 정보 등록 성공")
                .data(res)
                .build();
    }
}
