package com.green.core.application.grade.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GradeAppealProReq {

    @NotBlank
    private String status;          // "APPROVED" or "REJECTED"

    private String rejectReason;    // REJECTED 시 필수

    private Integer midScore;       // APPROVED 시 필수
    private Integer finScore;
    private Integer assignmentScore;
}
