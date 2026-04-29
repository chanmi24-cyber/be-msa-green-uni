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
public class StudentEvent implements Serializable {
    private String memberType;
    private Long memberCode;
    private String name;
    private EventType eventType;
}
