package com.green.member.application.professor.model;

import com.green.common.enumcode.EnumProfessorStatus;
import com.green.member.enumcode.EnumAdminStatus;
import com.green.member.enumcode.EnumProfessorPosition;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class StatusUpdateProfessorReq {
    private EnumProfessorStatus status;
    private EnumProfessorPosition position;
    private String reason;
    private LocalDate startDate;
    private LocalDate endDate;
}
