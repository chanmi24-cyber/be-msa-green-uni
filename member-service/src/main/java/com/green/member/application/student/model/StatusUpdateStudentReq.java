package com.green.member.application.student.model;

import com.green.common.enumcode.EnumProfessorStatus;
import com.green.common.enumcode.EnumStudentStatus;
import com.green.member.enumcode.EnumProfessorPosition;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class StatusUpdateStudentReq {
    @NotNull(message = "상태값은 필수입니다")
    private EnumStudentStatus status;
    private String reason;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer returnYear;
    private Integer returnSemester;
}
