package com.green.auth.application.email;

import com.green.auth.application.auth.AuthMemberRepository;
import com.green.auth.application.email.model.EmailSendReq;
import com.green.auth.application.email.model.EmailVerifyReq;
import com.green.auth.entity.AuthMember;
import com.green.common.exception.AuthErrorCode;
import com.green.common.exception.EmailErrorCode;
import com.green.common.exception.BusinessException;
import com.green.common.redis.RedisService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {
    private final AuthMemberRepository authMemberRepository;
    private final EmailSender emailSender;
    private final RedisService redisService;

    public void sendVerifyCode(EmailSendReq req) throws MessagingException {
        AuthMember authMember = authMemberRepository.findByMemberCodeAndEmail(req.getMemberCode(), req.getEmail());
        String email = req.getEmail().trim().toLowerCase();
        // 정보 불일치의 경우
        if (authMember == null) {
            throw new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND);
        }
        // 비활성 계정의 경우
        if (!authMember.getIsActive()) {
            throw new BusinessException(AuthErrorCode.INACTIVE_ACCOUNT);
        }
        int code = new SecureRandom().nextInt(90000) + 10000;
        String verifyCode = String.valueOf(code);
        redisService.save("EMAIL-VERIFY:" + email, verifyCode, 300);

        emailSender.sendHtmlMail(req.getEmail(), "[그린대학교] 비밀번호 재설정 인증코드", verifyCode);
    }

    public void checkVerifyCode(EmailVerifyReq req) {
        String email = req.getEmail().trim().toLowerCase();
        String savedCode = redisService.get("EMAIL-VERIFY:" + email, String.class);

        if (savedCode == null) {
            throw new BusinessException(EmailErrorCode.EXPIRED_VERIFY_CODE);
        }
        if (!savedCode.equals(req.getVerifyCode())) {
            throw new BusinessException(EmailErrorCode.INVALID_VERIFY_CODE);
        }
        redisService.save("EMAIL-VERIFIED:" + email, "true", 600);
        redisService.delete("EMAIL-VERIFY:" + email);
    }
}