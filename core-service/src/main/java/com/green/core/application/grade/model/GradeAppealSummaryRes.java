package com.green.core.application.grade.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GradeAppealSummaryRes {
    private long pendingCount;
    private long approvedCount;
    private long rejectedCount;
}