package com.green.academic.application.notification.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("isRead")
    private boolean isRead;
    private LocalDateTime createdAt;
    private String type;
    private Integer totalCount;
}