package com.green.common.kafka;

import com.green.common.constants.EventType;

public interface KafkaEvent {
    EventType getEventType();
}