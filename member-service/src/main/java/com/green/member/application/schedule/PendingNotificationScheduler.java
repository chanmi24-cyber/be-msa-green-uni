package com.green.member.application.schedule;

import com.green.common.constants.EventType;
import com.green.common.enumcode.EnumApprovalStatus;
import com.green.common.enumcode.EnumMemberRole;
import com.green.common.kafka.NotificationEvent;
import com.green.member.repository.MajorRequestRepository;
import com.green.member.repository.StatusRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PendingNotificationScheduler {

    private final MajorRequestRepository majorRequestRepository;
    private final StatusRequestRepository statusRequestRepository;
    private final NotificationProducer notificationProducer;

    @Scheduled(cron = "0 0 9 * * *") // 매일 오전 9시
    public void sendPendingNotifications() {
        sendMajorRequestAdminNotification();
        sendStatusRequestAdminNotification();
    }

    private void sendMajorRequestAdminNotification() {
        long count = majorRequestRepository.countByStatus(EnumApprovalStatus.PENDING);
        if (count > 0) {
            notificationProducer.sendNotification(NotificationEvent.builder()
                    .eventType(EventType.E_CREATED)
                    .targetRole(EnumMemberRole.ADMIN)
                    .type("MAJOR_REQUEST_PENDING")
                    .message("전공변경신청 대기가 " + count + "건 있습니다.")
                    .url("/admin/members/major-request")
                    .build());
        }
        log.info("전공변경신청 대기 관리자 알림: {}건", count);
    }

    private void sendStatusRequestAdminNotification() {
        long count = statusRequestRepository.countByStatus(EnumApprovalStatus.PENDING);
        if (count > 0) {
            notificationProducer.sendNotification(NotificationEvent.builder()
                    .eventType(EventType.E_CREATED)
                    .targetRole(EnumMemberRole.ADMIN)
                    .type("STATUS_REQUEST_PENDING")
                    .message("학적변동신청 대기가 " + count + "건 있습니다.")
                    .url("/admin/members/status-request")
                    .build());
        }
        log.info("학적변동신청 대기 관리자 알림: {}건", count);
    }
}