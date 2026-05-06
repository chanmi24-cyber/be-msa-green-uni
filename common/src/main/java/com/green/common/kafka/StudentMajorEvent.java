package com.green.common.kafka;

import com.green.common.constants.EventType;
import com.green.common.enumcode.EnumMajorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentMajorEvent implements Serializable {
    private Long studentMajorId;
    private Long studentCode;
    private Long majorId;
    private EnumMajorType type;
    private Boolean isActive;
    private EventType eventType;
}
