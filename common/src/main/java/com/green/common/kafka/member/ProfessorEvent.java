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
public class ProfessorEvent implements Serializable, KafkaEvent {
    private Long memberCode;
    private String name;
    private Long majorId;
    private String degree;
    private String status;
    private EventType eventType;
    private String updateType;
}