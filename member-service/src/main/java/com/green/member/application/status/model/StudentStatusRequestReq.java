package com.green.member.application.status.model;

import com.green.member.enumcode.EnumStatusRequestType;
import jakarta.validation.constraints.FutureOrPresent;
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
    @FutureOrPresent(message = "시작일은 과거 날짜로 입력할 수 없습니다.")
    private LocalDate startDate;

    // 휴학 시에만 필요
    private Integer returnYear;
    private Integer returnSemester;
}
