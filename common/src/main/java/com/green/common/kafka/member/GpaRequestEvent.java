package com.green.common.kafka.member;

import com.green.common.constants.EventType;
import com.green.common.kafka.KafkaEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GpaRequestEvent implements Serializable, KafkaEvent {
    private Long requestId;   // MajorRequest PK (응답 매칭용)
    private Long studentCode;
    private EventType eventType;
}
