package com.green.common.kafka.member;

import com.green.common.constants.EventType;
import com.green.common.enumcode.EnumStudentStatus;
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
    private String email;
    private Integer academicYear;
    private Integer semester;
    private String status;
    private Boolean isTransfer;
    private Boolean isMultiChild;
    private Boolean isVeteran;
    private EventType eventType;
}
