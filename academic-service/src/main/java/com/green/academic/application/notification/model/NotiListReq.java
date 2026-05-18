package com.green.academic.application.notification.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotiListReq {
    private Integer page;
    private Integer size;
    private Integer startIdx;
    private Boolean isRead;
}