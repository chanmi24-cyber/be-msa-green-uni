package com.green.member.application.student.model;

import com.green.common.enumcode.EnumStudentStatus;
import com.green.member.application.member.model.MemberCreateReq;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StudentCreateReq extends MemberCreateReq {

    @NotNull(message = "전공 ID는 필수입니다.")
    private Long majorId;

    @NotNull(message = "학년은 필수입니다.")
    @Min(value = 1, message = "학년은 1 이상이어야 합니다.")
    @Max(value = 6, message = "학년은 6 이하여야 합니다.")
    private Integer academicYear;

    @NotNull(message = "학기는 필수입니다.")
    @Min(value = 1, message = "학기는 1 또는 2여야 합니다.")
    @Max(value = 2, message = "학기는 1 또는 2여야 합니다.")
    private Integer semester;

    private Boolean isTransfer;
    private Boolean isMultiChild;
    private Boolean isVeteran;

    @NotNull(message = "현재 상태는 필수입니다.")
    private EnumStudentStatus status;
}
