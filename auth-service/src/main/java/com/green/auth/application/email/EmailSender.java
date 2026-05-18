package com.green.auth.application.email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSender {
    private final JavaMailSender mailSender;

    // 1. 기존 메서드: 본인인증 전용 (인증코드 템플릿 조립)
    public void sendHtmlMail(String to, String subject, String authCode) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);

        String htmlContent = getVerificationCodeTemplate(authCode);

        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    // 2. 새로 추가한 메서드: 외부 도메인(등록금 등)에서 가공해 온 HTML 본문을 그대로 발송
    public void sendRawHtmlMail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // core에서 넘겨받은 HTML 템플릿을 그대로 주입!

        mailSender.send(message);
    }

    private String getVerificationCodeTemplate(String authCode) {
        return """
        <div style="font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #fdfcfe; padding: 50px 10px;">
            <div style="max-width: 500px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; border: 1px solid #e0e0e0; overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.05);">
                <div style="padding: 30px; text-align: center; border-bottom: 1px solid #f0f0f0;">
                    <h1 style="color:#3e9e7e; margin: 0; font-size: 24px;">그린대학교</h1>
                    <p style="color: #666; margin: 10px 0 0 0; font-size: 18px; font-weight: 200;">본인 확인 인증코드</p>
                </div>
                <div style="padding: 30px 10px; text-align: center;">
                    <div style="background-color: #f8f9fa; border: 1px dashed #3e9e7e; border-radius: 8px; padding: 20px; margin-bottom: 30px;">
                        <span style="font-size: 32px; font-weight: bold; color: #3e9e7e; letter-spacing: 5px; line-height: 1;">%s</span>
                    </div>
                    <p style="font-size: 14px; color: #888;">요청하신 적이 없다면 이 메일을 무시하셔도 됩니다.</p>
                </div>
                <div style="padding: 20px; background-color: #fafafa; text-align: center; font-size: 12px; color: #999;">
                    © Green University. All rights reserved.<br>
                </div>
            </div>
        </div>
        """.formatted(authCode);
    }
}