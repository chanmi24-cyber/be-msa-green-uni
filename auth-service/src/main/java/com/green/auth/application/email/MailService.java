package com.green.auth.application.email;

import com.green.auth.application.auth.AuthMemberRepository;
import com.green.auth.application.email.model.EmailSendReq;
import com.green.auth.application.email.model.EmailVerifyReq;
import com.green.common.exception.AuthErrorCode;
import com.green.common.exception.EmailErrorCode;
import com.green.common.exception.BusinessException;
import com.green.common.redis.RedisService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {
    private final AuthMemberRepository authMemberRepository;
    private final EmailSender emailSender;
    private final RedisService redisService;

    public void sendVerifyCode(EmailSendReq req) throws MessagingException {
        boolean exists = authMemberRepository.existsByMemberCodeAndEmail(req.getMemberCode(), req.getEmail());
        if (!exists) {
            throw new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND);
        }
        String verifyCode = String.format("%05d", (int)(Math.random() * 90000) + 10000);
        redisService.save("EMAIL-VERIFY-CODE:" + req.getEmail().trim().toLowerCase(), verifyCode, 300);

        emailSender.sendHtmlMail(req.getEmail(), "[그린대학교] 비밀번호 재설정 인증코드", "인증코드: " + verifyCode);
    }

    public void checkVerifyCode(EmailVerifyReq req) {
        String savedCode = redisService.get("EMAIL-VERIFY:" + req.getEmail(), String.class);

        if (savedCode == null) {
            throw new BusinessException(EmailErrorCode.EXPIRED_VERIFY_CODE);
        }
        if (!savedCode.equals(req.getVerifyCode())) {
            throw new BusinessException(EmailErrorCode.INVALID_VERIFY_CODE);
        }
        redisService.save("EMAIL-VERIFIED:" + req.getEmail().trim().toLowerCase(), "true", 600);
    }
}