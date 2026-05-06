package com.green.common.kafka.member;

import com.green.common.constants.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessorEvent implements Serializable {
    private Long memberCode;
    private String name;
    private String degree;
    private String status;
    private EventType eventType;
}