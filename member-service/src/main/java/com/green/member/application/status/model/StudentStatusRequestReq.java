package com.green.member.application.status.model;

import com.green.member.enumcode.EnumStatusRequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentStatusRequestReq {
    @NotNull
    private EnumStatusRequestType type;

    @NotBlank
    private String reason;

    @NotNull
    private LocalDate startDate;

    // 휴학 시에만 필요
    private Integer returnYear;
    private Integer returnSemester;
}
