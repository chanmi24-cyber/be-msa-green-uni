package com.green.common.kafka;

import com.green.common.constants.EventType;
import com.green.common.enumcode.EnumMemberRole;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent implements KafkaEvent {
    private EventType eventType;
    private Long memberCode;       // 개인 알림 (전체면 null)
    private EnumMemberRole targetRole; // 전체 발송 대상 역할 (개인이면 null)
    private String type;           // LECTURE_APPROVED 등
    private String message;
    private String url;
    private Long refId;
}