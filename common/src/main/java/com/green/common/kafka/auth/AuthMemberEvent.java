package com.green.common.kafka.auth;

import com.green.common.constants.EventType;
import com.green.common.enumcode.EnumMemberRole;
import com.green.common.kafka.KafkaEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthMemberEvent implements Serializable, KafkaEvent {
    private Long memberCode;
    private String email;
    private String password;
    private String role;
    private EventType eventType;
}