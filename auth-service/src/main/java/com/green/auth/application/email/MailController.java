package com.green.auth.application.email;

import com.green.auth.application.email.model.EmailSendReq;
import com.green.auth.application.email.model.EmailVerifyReq;
import com.green.common.model.ResultResponse;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
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
    public ResultResponse<?> sendVerifyCode(@RequestBody @Valid EmailSendReq req) throws MessagingException {
        mailService.sendVerifyCode(req);
        return ResultResponse.builder()
                .message("인증코드가 발송 되었습니다")
                .build();
    }

    @PostMapping("/verification")
    public ResultResponse<?> checkVerifyCode(@RequestBody @Valid EmailVerifyReq req) {
        mailService.checkVerifyCode(req);
        return ResultResponse.builder()
                .message("본인 인증이 완료되었습니다")
                .build();
    }
}