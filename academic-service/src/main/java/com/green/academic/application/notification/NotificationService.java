package com.green.academic.application.notification;

import com.green.academic.application.notification.model.NotiListReq;
import com.green.academic.application.notification.model.NotiListRes;
import com.green.academic.application.notification.model.UnreadCountRes;
import com.green.academic.entity.Notification;
import com.green.common.model.MemberDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    // NOTI-01 알림 목록 조회
    public List<NotiListRes> getNotifications(MemberDto memberDto, NotiListReq req) {
        if (req.getPage() != null && req.getSize() != null) {
            req.setStartIdx((req.getPage() - 1) * req.getSize());
        }
        return notificationMapper.findNotifications(memberDto.memberCode(), memberDto.role().getCode(), req);
    }

    // NOTI-02 미읽음 알림 개수
    public UnreadCountRes getUnreadCount(MemberDto memberDto) {
        long count = notificationRepository.countUnreadByMemberCodeOrRole(
                memberDto.memberCode(), memberDto.role().getCode());
        return UnreadCountRes.builder()
                .unreadCount((int) count)
                .build();
    }

    // NOTI-03 알림 읽음 처리
    @Transactional
    public void readNotification(MemberDto memberDto, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다."));

        if (notification.getMemberCode() != null
                && !notification.getMemberCode().equals(memberDto.memberCode())) {
            throw new IllegalArgumentException("본인 알림이 아닙니다.");
        }

        notification.read();
    }

    // NOTI-04 전체 읽음 처리
    @Transactional
    public void readAllNotifications(MemberDto memberDto) {
        notificationRepository.readAllByMemberCode(memberDto.memberCode());
    }

    // NOTI-06 알림 삭제
    @Transactional
    public void deleteNotification(MemberDto memberDto, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다."));

        if (notification.getMemberCode() != null
                && !notification.getMemberCode().equals(memberDto.memberCode())) {
            throw new IllegalArgumentException("본인 알림이 아닙니다.");
        }

        notificationRepository.delete(notification);
    }

    // NOTI-07 전체 삭제
    @Transactional
    public void deleteAllNotifications(MemberDto memberDto) {
        notificationRepository.deleteAllByMemberCode(memberDto.memberCode());
    }
}