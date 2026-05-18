package com.green.core.application.tuition.model;

import com.green.core.entity.tuition.Tuition;
import com.green.core.enumcode.EnumTuitionStatus;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class TuitionRes {
    private final Long tuitionId;
    private final Long studentCode;
    private final Integer year;
    private final Integer semester;
    private final String collegeName;
    private final Long baseAmount;
    private final Long totalDiscount;
    private final Long finalAmount;
    private final EnumTuitionStatus status;
    private final LocalDateTime paidAt;
    private final LocalDateTime deadline;

    public TuitionRes(Tuition tuition) {
        this.tuitionId = tuition.getTuitionId();
        this.studentCode = tuition.getStudentCode();
        this.year = tuition.getYear();
        this.semester = tuition.getSemester();
        this.collegeName = tuition.getTuitionPolicy().getCollege().getName();
        this.baseAmount = tuition.getBaseAmount();
        this.totalDiscount = tuition.getTotalDiscount();
        this.finalAmount = tuition.getFinalAmount();
        this.status = tuition.getStatus();
        this.paidAt = tuition.getPaidAt();
        this.deadline = tuition.getDeadline();
    }
}
