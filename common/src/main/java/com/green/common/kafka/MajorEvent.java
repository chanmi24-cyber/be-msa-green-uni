package com.green.common.kafka;

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
public class MajorEvent implements Serializable {
    private Long majorId;
    private String name;
    private String collegeName;
    private EventType eventType;
}
