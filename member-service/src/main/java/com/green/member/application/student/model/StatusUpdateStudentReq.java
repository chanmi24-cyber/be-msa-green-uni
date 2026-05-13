package com.green.member.application.student.model;

import com.green.common.enumcode.EnumProfessorStatus;
import com.green.common.enumcode.EnumStudentStatus;
import com.green.member.enumcode.EnumProfessorPosition;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class StatusUpdateStudentReq {
    private EnumStudentStatus status;
    private String reason;
    private LocalDate startDate;
    private LocalDate endDate;
}
