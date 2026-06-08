package com.green.core.application.tuition;

import com.green.core.entity.tuition.Tuition;
import com.green.core.entity.tuition.TuitionMailLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TuitionMailAsyncService {

    private final EmailSender emailSender;
    private final TuitionMailLogRepository tuitionMailLogRepository;

    @Async("mailAsyncExecutor") // 💡 비동기 스레드 풀에서 실행
    @Transactional // 💡 개별 메일 전송 및 로그 저장을 하나의 독립된 트랜잭션으로 관리
    public void sendEmailAndLog(Tuition tuition, String studentEmail, String mailTitle, String mailContent, Long adminCode) {
        boolean isSuccess = true;
        try {
            // 외부 SMTP 서버 통신 (시간이 오래 걸리는 작업)
            emailSender.sendRawHtmlMail(studentEmail, mailTitle, mailContent);
            log.info("등록금 미납 메일 발송 성공 - 학생코드: {}, 이메일: {}", tuition.getStudentCode(), studentEmail);
        } catch (Exception e) {
            isSuccess = false;
            log.error("등록금 미납 메일 발송 실패 - 학생코드: {}, 사유: {}", tuition.getStudentCode(), e.getMessage());
        }

        // 비동기 스레드 내의 트랜잭션에서 로그 저장
        tuitionMailLogRepository.save(TuitionMailLog.builder()
                .tuition(tuition)
                .recipientEmail(studentEmail)
                .isSuccess(isSuccess)
                .senderCode(adminCode)
                .build());
    }
}