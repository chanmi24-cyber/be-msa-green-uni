package com.green.common.kafka.member;

import com.green.common.constants.EventType;
import com.green.common.constants.UpdateType;
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
public class StudentEvent implements Serializable, KafkaEvent {
    private String memberType;
    private Long memberCode;
    private String name;
    private String email;
    private Integer academicYear;
    private Integer semester;
    private Long majorId;
    private Long minorId;
    private String status;
    private Boolean isTransfer;
    private Boolean isMultiChild;
    private Boolean isVeteran;
    private EventType eventType;
    private UpdateType updateType;
}
