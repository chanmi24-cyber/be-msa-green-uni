package com.green.member.application.professor.model;

import com.green.common.enumcode.EnumProfessorStatus;
import com.green.member.enumcode.EnumProfessorPosition;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@ToString
public class ProfessorHistoryRes {
    private String changeType;
    EnumProfessorStatus oldStatus;
    EnumProfessorStatus newStatus;
    EnumProfessorPosition oldPosition;
    EnumProfessorPosition newPosition;
    LocalDate startDate;
    LocalDate endDate;
    String reason;
    LocalDateTime createdAt;
}
