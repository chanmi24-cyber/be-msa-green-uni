package com.green.common.kafka;

import com.green.common.constants.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * AuthService에서 발생한 변경 사항을 담는 이벤트 DTO
 * Kafka를 통해 타 서비스(Order 등)로 전달됩니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberEvent implements Serializable {

    private Integer memberCode;

    private EventType eventType;
}
