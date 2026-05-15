package com.green.academic.application.notification;

import com.green.academic.application.notification.model.NotiListReq;
import com.green.academic.application.notification.model.NotiListRes;
import com.green.academic.application.notification.model.UnreadCountRes;
import com.green.common.auth.MemberContext;
import com.green.common.model.MemberDto;
import com.green.common.model.ResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    // NOTI-01 알림 목록 조회
    @GetMapping
    public ResultResponse<List<NotiListRes>> getNotifications(@ModelAttribute NotiListReq req) {
        MemberDto memberDto = MemberContext.get();
        return ResultResponse.<List<NotiListRes>>builder()
                .message("알림 목록 조회 성공")
                .data(notificationService.getNotifications(memberDto, req))
                .build();
    }

    // NOTI-02 미읽음 알림 개수
    @GetMapping("/unread-count")
    public ResultResponse<UnreadCountRes> getUnreadCount() {
        MemberDto memberDto = MemberContext.get();
        return ResultResponse.<UnreadCountRes>builder()
                .message("미읽음 알림 개수 조회 성공")
                .data(notificationService.getUnreadCount(memberDto))
                .build();
    }

    // NOTI-03 알림 읽음 처리
    @PostMapping("/{notificationId}/read")
    public ResultResponse<Void> readNotification(@PathVariable Long notificationId) {
        MemberDto memberDto = MemberContext.get();
        notificationService.readNotification(memberDto, notificationId);
        return ResultResponse.<Void>builder().message("읽음 처리 성공").build();
    }

    // NOTI-04 전체 읽음 처리
    @PostMapping("/read-all")
    public ResultResponse<Void> readAllNotifications() {
        MemberDto memberDto = MemberContext.get();
        notificationService.readAllNotifications(memberDto);
        return ResultResponse.<Void>builder().message("전체 읽음 처리 성공").build();
    }

    // NOTI-05 알림 삭제
    @DeleteMapping("/{notificationId}")
    public ResultResponse<Void> deleteNotification(@PathVariable Long notificationId) {
        MemberDto memberDto = MemberContext.get();
        notificationService.deleteNotification(memberDto, notificationId);
        return ResultResponse.<Void>builder().message("알림 삭제 성공").build();
    }

    // NOTI-06 전체 삭제
    @DeleteMapping
    public ResultResponse<Void> deleteAllNotifications() {
        MemberDto memberDto = MemberContext.get();
        notificationService.deleteAllNotifications(memberDto);
        return ResultResponse.<Void>builder().message("전체 삭제 성공").build();
    }
}