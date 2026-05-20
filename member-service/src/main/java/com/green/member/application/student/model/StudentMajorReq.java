package com.green.member.application.student.model;

import com.green.member.enumcode.EnumMajorRequestType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentMajorReq {
    @NotNull
    private EnumMajorRequestType type;
    @NotNull
    private Long targetMajorId;
    private String reason;
}
