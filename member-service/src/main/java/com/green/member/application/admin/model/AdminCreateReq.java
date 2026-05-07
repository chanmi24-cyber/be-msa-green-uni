package com.green.member.application.admin.model;

import com.green.member.application.member.model.MemberCreateReq;
import com.green.member.enumcode.EnumAdminStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdminCreateReq extends MemberCreateReq {
    private EnumAdminStatus status;
}
