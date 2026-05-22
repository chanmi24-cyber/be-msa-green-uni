package com.green.core.application.schedule;

import com.green.common.constants.EventType;
import com.green.common.enumcode.EnumApprovalStatus;
import com.green.common.enumcode.EnumMemberRole;
import com.green.common.enumcode.EnumScheduleType;
import com.green.common.kafka.NotificationEvent;
import com.green.core.application.lecture.repository.EvaluationRepository;
import com.green.core.application.lecture.repository.LectureRepository;
import com.green.core.kafka.NotificationProducer;
import com.green.core.repository.*;
import com.green.core.repository.ScheduleCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PendingNotificationScheduler {

    private final ScheduleCacheRepository scheduleCacheRepository;
    private final EvaluationRepository evaluationRepository;
    private final GradeRepository gradeRepository;
    private final GradesAppealRepository gradesAppealRepository;
    private final TuitionRepository tuitionRepository;
    private final LectureRepository lectureRepository;
    private final NotificationProducer notificationProducer;

    @Scheduled(cron = "0 0 9 * * *") // 매일 오전 9시
    public void sendPendingNotifications() {
        LocalDate today = LocalDate.now();

        // 강의평가 기간 활성 여부 체크
        boolean evalActive = scheduleCacheRepository
                .findByTypeAndIsActiveTrue(EnumScheduleType.LECTURE_EVALUATION).isPresent();
        if (evalActive) {
            sendEvalNotifications();
        }

        // 성적입력 기간 활성 여부 체크
        boolean gradeInputActive = scheduleCacheRepository
                .findByTypeAndIsActiveTrue(EnumScheduleType.GRADE_INPUT).isPresent();
        if (gradeInputActive) {
            sendGradeInputNotifications();
        }

        // 성적이의신청 미처리 (기간 무관, 항상 체크)
        sendGradeAppealNotifications();

        // 등록금 미납 (기간 활성 체크)
        boolean tuitionActive = scheduleCacheRepository
                .findByTypeAndIsActiveTrue(EnumScheduleType.TUITION_PAYMENT).isPresent();
        if (tuitionActive) {
            sendTuitionNotifications();
        }

        // 관리자: 강의개설 승인 대기
        sendLecturePendingAdminNotification();
    }

    private void sendEvalNotifications() {
        List<Long> studentCodes = lectureEval