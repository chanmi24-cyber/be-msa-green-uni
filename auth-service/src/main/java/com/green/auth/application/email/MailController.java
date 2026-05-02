package com.green.auth.application.email;

import com.green.auth.application.email.model.EmailSendReq;
import com.green.auth.application.email.model.EmailVerifyReq;
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
@RequestMapping("/emails")
public class MailController {
    private final MailService mailService;

    @PostMapping
    public ResultResponse<?> sendVerifyCode(@RequestBody EmailSendReq req) {
        mailService.sendVerifyCode(req);
        return ResultResponse.builder()
                .message("인증코드 발송 완료")
                .data(null)
                .build();
    }

    @PostMapping("/verification")
    public ResultResponse<?> checkVerifyCode(@RequestBody EmailVerifyReq req) {
        mailService.checkVerifyCode(req);
        return ResultResponse.builder()
                .message("인증코드 확인 완료")
                .data(null)
                .build();
    }
}