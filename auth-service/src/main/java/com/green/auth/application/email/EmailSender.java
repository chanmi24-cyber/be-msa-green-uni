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

    public void sendMail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }

    public void sendHtmlMail(String to, String subject, String authCode) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);

        String htmlContent = getVerificationCodeTemplate(authCode);

        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    private String getVerificationCodeTemplate(String authCode) {
        return "<div style=\"font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #fdfcfe; padding: 50px 10px;\">\n" +
                "    <div style=\"max-width: 500px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; border: 1px solid #e0e0e0; overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.05);\">\n" +
                "        \n" +
                "        <div style=\"padding: 30px; text-align: center; border-bottom: 1px solid #f0f0f0;\">\n" +
                "            <h1 style=\"color:#3e9e7e; margin: 0; font-size: 24px;\">그린대학교</h1>\n" +
                "            <p style=\"color: #666; margin: 10px 0 0 0; font-size: 18px; font-weight: 200;\">본인 확인 인증코드</p>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div style=\"padding: 40px 30px; text-align: center;\">\n" +
                "            <p style=\"font-size: 16px; color: #333; line-height: 1.6; margin-bottom: 30px;\">\n" +
                "                아래 인증 코드를 입력해주세요.                \n" +
                "            </p>            \n" +
                "            <div style=\"background-color: #f8f9fa; border: 1px dashed #3e9e7e; border-radius: 8px; padding: 20px; margin-bottom: 30px;\">\n" +
                "                <span style=\"font-size: 32px; font-weight: bold; color: #3e9e7e; letter-spacing: 5px; line-height: 1;\">\" + authCode + \"</span>\n" +
                "            </div>\n" +
                "            <p style=\"font-size: 14px; color: #888;\">요청하신 적이 없다면 이 메일을 무시하셔도 됩니다.</p>\n" +
                "        </div>\n" +
                "\n" +
                "        <div style=\"padding: 20px; background-color: #fafafa; text-align: center; font-size: 12px; color: #999;\">\n" +
                "            © Green University. All rights reserved.<br>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</div>";
    }
}