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
public class GraduationCheckRequestEvent implements Serializable, KafkaEvent {
    private Long studentCode;
    @Builder.Default
    private EventType eventType = EventType.E_CREATED;
}
