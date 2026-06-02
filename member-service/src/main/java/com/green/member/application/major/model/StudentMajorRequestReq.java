package com.green.member.application.major.model;

import com.green.member.enumcode.EnumMajorRequestType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentMajorRequestReq {
    @NotNull
    private EnumMajorRequestType type;
    @NotNull
    private Long targetMajorId;
    private String reason;
}
