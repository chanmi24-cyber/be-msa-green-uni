package com.green.member.application.student.model;

import com.green.common.enumcode.EnumStudentStatus;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@ToString
public class StudentHistoryRes {
    String changeType;
    EnumStudentStatus oldStatus;
    EnumStudentStatus newStatus;
    LocalDate startDate;
    LocalDate endDate;
    String reason;
    Integer returnYear;
    Integer returnSemester;
    LocalDateTime createdAt;
}
