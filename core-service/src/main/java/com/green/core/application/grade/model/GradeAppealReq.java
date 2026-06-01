package com.green.core.application.grade.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// API-GPA-07: 학생 성적 이의신청 요청
@Getter
@NoArgsConstructor
public class GradeAppealReq {

    @NotBlank(message = "이의신청 내용을 입력해주세요.")
    @Size(max = 1000, message = "이의신청 내용은 1000자 이내로 작성해주세요.")
    private String reason;
}