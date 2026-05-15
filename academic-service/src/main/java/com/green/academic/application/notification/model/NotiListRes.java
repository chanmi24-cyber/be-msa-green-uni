package com.green.academic.application.notification.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotiListRes {
    private Long notiId;
    private Long refId;
    private String url;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
    private String type;
    private Integer totalCount;
}