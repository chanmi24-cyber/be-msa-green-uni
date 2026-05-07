package com.green.core.application.attendance.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor //생성자가 없었어서 이걸 추가해야 활성화
public class AttendSessionEndRes {
    private Long sessionId;
    private Boolean isActive;
    private LocalDateTime endedAt;
}
