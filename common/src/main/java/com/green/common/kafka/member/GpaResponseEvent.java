package com.green.common.kafka.member;

import com.green.common.constants.EventType;
import com.green.common.kafka.KafkaEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GpaResponseEvent implements Serializable, KafkaEvent {
    private Long requestId;       // 요청 PK (매칭용)
    private String requestType;   // "MAJOR_REQUEST" | "STATUS_REQUEST"
    private BigDecimal gpa;
    private Integer totalCredits;
    private EventType eventType;
}
