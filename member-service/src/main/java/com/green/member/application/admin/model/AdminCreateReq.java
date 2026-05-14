package com.green.member.application.admin.model;

import com.green.member.application.member.model.MemberCreateReq;
import com.green.member.enumcode.EnumAdminStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class AdminCreateReq extends MemberCreateReq {
    private EnumAdminStatus status;
}
