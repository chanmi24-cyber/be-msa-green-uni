package com.green.common.kafka;

import com.green.common.constants.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// CoreService에서 발생한 변경 사항을 담는 이벤트 DTO
// Kafka를 통해 타 서비스로 전달
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MajorEvent implements Serializable {
    private Long majorId;
    private String name;
    private EventType eventType;
}
