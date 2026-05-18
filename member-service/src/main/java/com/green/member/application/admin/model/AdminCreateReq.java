package com.green.member.application.admin.model;

import com.green.member.application.member.model.MemberCreateReq;
import com.green.member.enumcode.EnumAdminStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class AdminCreateReq extends MemberCreateReq {
    @NotNull(message = "현재 상태는 필수입니다.")
    private EnumAdminStatus status;
}
