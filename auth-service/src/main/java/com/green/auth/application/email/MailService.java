package com.green.auth.application.email;

import com.green.auth.application.AuthMemberRepository;
import com.green.auth.application.email.model.EmailSendReq;
import com.green.auth.application.email.model.EmailVerifyReq;
import com.green.common.email.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {
    private final AuthMemberRepository authMemberRepository;
    private final EmailSender emailSender;

    private final Map<String, String> codeStore = new HashMap<>();

    public void sendVerifyCode(EmailSendReq req) {
        boolean exists = authMemberRepository.existsByMemberCodeAndEmail(req.getMemberCode(), req.getEmail());
        if (!exists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 회원입니다");
        }

        String verifyCode = String.format("%05d", (int)(Math.random() * 90000) + 10000);
        codeStore.put(req.getEmail(), verifyCode);

        emailSender.sendMail(req.getEmail(), "[그린대학교] 비밀번호 재설정 인증코드", "인증코드: " + verifyCode);
    }

    public void checkVerifyCode(EmailVerifyReq req) {
        String savedCode = codeStore.get(req.getEmail());

        if (savedCode == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "인증코드가 만료되었습니다.");
        }
        if (!savedCode.equals(req.getVerifyCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "인증코드가 일치하지 않습니다.");
        }

        codeStore.remove(req.getEmail());
    }
}