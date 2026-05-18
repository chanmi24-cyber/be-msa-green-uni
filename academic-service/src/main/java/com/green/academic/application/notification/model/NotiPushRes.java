package com.green.academic.application.notification.model;

import com.green.academic.entity.Notification;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotiPushRes {
    private Long notiId;
    private String type;
    private String message;
    private String url;
    private Long refId;
    private LocalDateTime createdAt;

    public static NotiPushRes from(Notification notification) {
        return NotiPushRes.builder()
                .notiId(notification.getNotiId())
                .type(notification.getType())
                .message(notification.getMessage())
                .url(notification.getUrl())
                .refId(notification.getRefId())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
